package sys.azentic.autumn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sys.azentic.autumn.dto.request.TransferRequest;
import sys.azentic.autumn.dto.response.TransferResponse;

import java.util.UUID;

/**
 * Controlador REST para operaciones de transferencias.
 * 
 * Endpoints:
 * - POST   /api/v1/transfers          - Crear nueva transferencia
 * - GET    /api/v1/transfers/{id}     - Consultar transferencia por ID
 * - GET    /api/v1/transfers/account/{accountId} - Listar transferencias de una cuenta
 */
@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
@Slf4j
public class TransferController {

    // TODO: Inyectar TransferService cuando se implemente
    // private final TransferService transferService;

    /**
     * Crea una nueva transferencia.
     * 
     * @param request Datos de la transferencia con validaciones
     * @return Respuesta con el estado de la transferencia creada
     */
    @PostMapping
    public ResponseEntity<TransferResponse> createTransfer(@Valid @RequestBody TransferRequest request) {
        log.info("Solicitud de transferencia recibida. IdempotencyKey: {}", request.getIdempotencyKey());
        
        // TODO: Implementar lógica de negocio
        // TransferResponse response = transferService.createTransfer(request);
        
        // Placeholder temporal
        TransferResponse response = TransferResponse.builder()
            .id(UUID.randomUUID())
            .idempotencyKey(request.getIdempotencyKey())
            .sourceAccountNumber(request.getSourceAccountNumber())
            .destinationAccountNumber(request.getDestinationAccountNumber())
            .amount(request.getAmount())
            .description(request.getDescription())
            .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Consulta una transferencia por su ID.
     * 
     * @param id ID de la transferencia
     * @return Datos de la transferencia
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransferResponse> getTransferById(@PathVariable UUID id) {
        log.info("Consultando transferencia con ID: {}", id);
        
        // TODO: Implementar lógica
        // TransferResponse response = transferService.getTransferById(id);
        
        return ResponseEntity.ok().build();
    }

    /**
     * Lista todas las transferencias de una cuenta (origen o destino).
     * 
     * @param accountId ID de la cuenta
     * @return Lista de transferencias
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<?> getTransfersByAccount(@PathVariable UUID accountId) {
        log.info("Consultando transferencias para cuenta ID: {}", accountId);
        
        // TODO: Implementar lógica
        // List<TransferResponse> transfers = transferService.getTransfersByAccount(accountId);
        
        return ResponseEntity.ok().build();
    }
}
