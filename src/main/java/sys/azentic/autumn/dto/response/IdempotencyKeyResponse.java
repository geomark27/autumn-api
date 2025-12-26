package sys.azentic.autumn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO para retornar una clave de idempotencia generada.
 * Se usa para que el cliente obtenga un UUID único antes de hacer la transferencia.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyKeyResponse {
    
    /**
     * UUID único para usar en la transferencia.
     * Debe enviarse en el campo "idempotencyKey" del TransferRequest.
     */
    private UUID idempotencyKey;
    
    /**
     * Mensaje explicativo para el cliente.
     */
    private String message;
}
