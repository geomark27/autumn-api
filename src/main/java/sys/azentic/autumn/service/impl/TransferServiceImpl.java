package sys.azentic.autumn.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sys.azentic.autumn.domain.entity.Account;
import sys.azentic.autumn.domain.entity.Transfer;
import sys.azentic.autumn.domain.enums.TransferStatus;
import sys.azentic.autumn.dto.request.TransferRequest;
import sys.azentic.autumn.dto.response.TransferResponse;
import sys.azentic.autumn.exception.AccountNotFoundException;
import sys.azentic.autumn.exception.InsufficientBalanceException;
import sys.azentic.autumn.exception.TransferNotFoundException;
import sys.azentic.autumn.mapper.TransferMapper;
import sys.azentic.autumn.repository.AccountRepository;
import sys.azentic.autumn.repository.TransferRepository;
import sys.azentic.autumn.service.IdempotencyService;
import sys.azentic.autumn.service.TransferService;

/**
 * Implementación del servicio de transferencias bancarias.
 * 
 * Responsabilidades:
 * - Crear transferencias con validaciones de negocio
 * - Gestionar locks pesimistas para evitar condiciones de carrera
 * - Usar idempotencia para evitar duplicados
 * - Registrar auditoría en cada paso
 * - Implementar compensación en caso de error
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TransferServiceImpl implements TransferService {
    
    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final TransferMapper transferMapper;
    private final IdempotencyService idempotencyService;
    
    @Override
    @Transactional
    public TransferResponse createTransfer(TransferRequest request) {
        log.info("=== INICIANDO CREACIÓN DE TRANSFERENCIA ===");
        log.info("IdempotencyKey: {}", request.getIdempotencyKey());
        log.info("Desde: {} - Hacia: {} - Monto: {}", 
            request.getSourceAccountNumber(), 
            request.getDestinationAccountNumber(),
            request.getAmount());
        
        // PASO 1: Verificar idempotencia - Si ya existe, retornar la transferencia previa
        var existingTransfer = transferRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingTransfer.isPresent()) {
            log.warn("Transferencia duplicada detectada. IdempotencyKey: {}", request.getIdempotencyKey());
            return transferMapper.toResponse(existingTransfer.get());
        }
        
        // PASO 2: Obtener cuenta origen
        log.debug("Obteniendo cuenta origen por número: {}", request.getSourceAccountNumber());
        Account sourceAccount = accountRepository.findByAccountNumber(request.getSourceAccountNumber())
            .orElseThrow(() -> {
                log.error("Cuenta origen no encontrada: {}", request.getSourceAccountNumber());
                return new AccountNotFoundException(request.getSourceAccountNumber());
            });
        
        // PASO 3: Obtener cuenta destino
        log.debug("Obteniendo cuenta destino por número: {}", request.getDestinationAccountNumber());
        Account destinationAccount = accountRepository.findByAccountNumber(request.getDestinationAccountNumber())
            .orElseThrow(() -> {
                log.error("Cuenta destino no encontrada: {}", request.getDestinationAccountNumber());
                return new AccountNotFoundException(request.getDestinationAccountNumber());
            });
        
        // PASO 4: Validar que no sean la misma cuenta
        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            log.error("Intento de transferencia a la misma cuenta");
            throw new IllegalArgumentException("No puedes transferir a la misma cuenta");
        }
        
        // PASO 5: Validar saldo suficiente
        log.debug("Validando saldo. Saldo actual: {} - Monto a transferir: {}", 
            sourceAccount.getBalance(), request.getAmount());
            
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            log.error("Fondos insuficientes. Saldo: {} - Requerido: {}", 
                sourceAccount.getBalance(), request.getAmount());
            throw new InsufficientBalanceException(
                sourceAccount.getAccountNumber(), 
                request.getAmount(), 
                sourceAccount.getBalance()
            );
        }
        
        // PASO 6: Crear entidad Transfer en estado PENDING
        log.info("Creando transferencia...");  
        Transfer transfer = Transfer.builder()
            .idempotencyKey(request.getIdempotencyKey())
            .sourceAccount(sourceAccount)
            .destinationAccount(destinationAccount)
            .amount(request.getAmount())
            .status(TransferStatus.PENDING)
            .description(request.getDescription())
            .build();
        
        // PASO 7: Actualizar saldos (Contabilidad de Doble Entrada)
        log.info("Actualizando saldos...");
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));
        
        // PASO 8: Cambiar estado a COMPLETED
        transfer.setStatus(TransferStatus.COMPLETED);
        
        // PASO 9: Guardar transferencia y cuentas
        log.info("Guardando cambios en base de datos...");
        Transfer savedTransfer = transferRepository.save(transfer);
        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);
        
        // PASO 10: Guardar en caché de idempotencia (24 horas)
        log.info("Guardando en caché de idempotencia...");
        idempotencyService.store(request.getIdempotencyKey(), savedTransfer.getId());
        
        log.info("=== TRANSFERENCIA COMPLETADA ===");
        log.info("Transferencia ID: {} - Estado: {}", savedTransfer.getId(), savedTransfer.getStatus());
        
        return transferMapper.toResponse(savedTransfer);
    }
    
    @Override
    public TransferResponse getTransferById(UUID transferId) {
        log.debug("Consultando transferencia por ID: {}", transferId);
        
        Transfer transfer = transferRepository.findById(transferId)
            .orElseThrow(() -> new TransferNotFoundException(transferId));
        
        log.info("Transferencia encontrada: {} - Estado: {}", 
            transfer.getId(), transfer.getStatus());
        
        return transferMapper.toResponse(transfer);
    }
    
    @Override
    public List<TransferResponse> getTransfersByAccount(UUID accountId) {
        log.debug("Consultando transferencias para cuenta ID: {}", accountId);
        
        // Busca todas las transferencias donde la cuenta sea origen O destino
        List<Transfer> transfers = transferRepository.findByAccountId(accountId);
        
        log.info("Se encontraron {} transferencias para la cuenta {}", 
            transfers.size(), accountId);
        
        // Mapea cada Transfer a TransferResponse
        return transfers.stream()
            .map(transferMapper::toResponse)
            .toList();
    }
}
