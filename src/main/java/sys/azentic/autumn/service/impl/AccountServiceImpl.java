package sys.azentic.autumn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sys.azentic.autumn.domain.entity.Account;
import sys.azentic.autumn.dto.response.AccountResponse;
import sys.azentic.autumn.exception.AccountNotFoundException;
import sys.azentic.autumn.mapper.AccountMapper;
import sys.azentic.autumn.repository.AccountRepository;
import sys.azentic.autumn.service.AccountService;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Implementación del servicio de cuentas bancarias.
 * 
 * Patrón utilizado:
 * - @Transactional(readOnly = true): Todas las operaciones son de solo lectura
 * - Uso de Optional con orElseThrow para manejo de errores
 * - Mapeo automático de Entity -> DTO usando MapStruct
 * 
 * Este servicio NO modifica cuentas, solo consulta.
 * Las modificaciones se hacen en TransferService con locks pesimistas.
 */
@Service
@Transactional(readOnly = true) // Todas las operaciones de este servicio son de lectura
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    public AccountResponse getAccountById(UUID accountId) {
        log.debug("Consultando cuenta por ID: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        
        log.info("Cuenta encontrada: {} - Saldo: {}", account.getAccountNumber(), account.getBalance());
        
        return accountMapper.toResponse(account);
    }

    @Override
    public AccountResponse getAccountByNumber(String accountNumber) {
        log.debug("Consultando cuenta por número: {}", accountNumber);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
        
        log.info("Cuenta encontrada: {} - Saldo: {}", account.getAccountNumber(), account.getBalance());
        
        return accountMapper.toResponse(account);
    }

    @Override
    public BigDecimal getBalance(UUID accountId) {
        log.debug("Consultando saldo de cuenta: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        
        BigDecimal balance = account.getBalance();
        
        log.info("Saldo de cuenta {}: {}", account.getAccountNumber(), balance);
        
        return balance;
    }
}
