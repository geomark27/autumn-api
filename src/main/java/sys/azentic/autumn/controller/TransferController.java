package sys.azentic.autumn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sys.azentic.autumn.dto.request.TransferRequest;
import sys.azentic.autumn.dto.response.IdempotencyKeyResponse;
import sys.azentic.autumn.dto.response.TransferResponse;
import sys.azentic.autumn.service.TransferService;

import java.util.List;
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

    private final TransferService transferService;

    /**
     * Genera un nuevo UUID para idempotencia.
     * 
     * Usar este endpoint ANTES de hacer una transferencia:
     * 1. GET /api/v1/transfers/generate-key → Obtiene UUID
     * 2. POST /api/v1/transfers → Envía transferencia con ese UUID
     * 
     * @return UUID único para usar en la próxima transferencia
     */
    @GetMapping("/generate-key")
    public ResponseEntity<IdempotencyKeyResponse> generateIdempotencyKey() {
        UUID newKey = UUID.randomUUID();
        log.debug("Nueva clave de idempotencia generada: {}", newKey);
        
        IdempotencyKeyResponse response = IdempotencyKeyResponse.builder()
            .idempotencyKey(newKey)
            .message("Use esta clave en el campo 'idempotencyKey' de la próxima transferencia")
            .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Crea una nueva transferencia.
     * 
     * @param request Datos de la transferencia con validaciones
     * @return Respuesta con el estado de la transferencia creada
     */
    @PostMapping
    public ResponseEntity<TransferResponse> createTransfer(@Valid @RequestBody TransferRequest request) {
        log.info("Solicitud de transferencia recibida. IdempotencyKey: {}", request.getIdempotencyKey());
        
        TransferResponse response = transferService.createTransfer(request);
        
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
        
        TransferResponse response = transferService.getTransferById(id);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todas las transferencias de una cuenta (origen o destino).
     * 
     * @param accountId ID de la cuenta
     * @return Lista de transferencias
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransferResponse>> getTransfersByAccount(@PathVariable UUID accountId) {
        log.info("Consultando transferencias para cuenta ID: {}", accountId);
        
        List<TransferResponse> transfers = transferService.getTransfersByAccount(accountId);
        
        return ResponseEntity.ok(transfers);
    }
}
