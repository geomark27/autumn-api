package sys.azentic.autumn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sys.azentic.autumn.domain.enums.TransferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para una transferencia.
 * Incluye toda la información relevante para el cliente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferResponse {

    private UUID id;
    private UUID idempotencyKey;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private TransferStatus status;
    private String description;
    private String errorMessage;
    private Boolean requiresApproval;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
