package sys.azentic.autumn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO estándar para respuestas de error.
 * Sigue el patrón RFC 7807 (Problem Details for HTTP APIs).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    
    /**
     * Detalles adicionales del error (opcional)
     * Por ejemplo, errores de validación campo por campo
     */
    private Map<String, String> details;

    public static ErrorResponse of(int status, String error, String message, String path) {
        return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status)
            .error(error)
            .message(message)
            .path(path)
            .build();
    }
}
