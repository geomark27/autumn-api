package sys.azentic.autumn.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para solicitar una nueva transferencia.
 * Incluye todas las validaciones a nivel de entrada.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferRequest {

    /**
     * Clave de idempotencia única.
     * El cliente debe generar un UUID único por transferencia.
     */
    @NotNull(message = "La clave de idempotencia es obligatoria")
    private UUID idempotencyKey;

    @NotBlank(message = "El número de cuenta de origen es obligatorio")
    @Size(min = 10, max = 20, message = "El número de cuenta debe tener entre 10 y 20 caracteres")
    private String sourceAccountNumber;

    @NotBlank(message = "El número de cuenta de destino es obligatorio")
    @Size(min = 10, max = 20, message = "El número de cuenta debe tener entre 10 y 20 caracteres")
    private String destinationAccountNumber;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "1.00", message = "El monto mínimo es 1.00")
    @DecimalMax(value = "999999999.99", message = "El monto excede el límite permitido")
    @Digits(integer = 15, fraction = 4, message = "El monto tiene un formato inválido")
    private BigDecimal amount;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;

    /**
     * Validación personalizada: las cuentas no pueden ser la misma
     */
    @AssertTrue(message = "Las cuentas de origen y destino deben ser diferentes")
    public boolean isDifferentAccounts() {
        if (sourceAccountNumber == null || destinationAccountNumber == null) {
            return true; // Dejar que @NotBlank maneje estos casos
        }
        return !sourceAccountNumber.equals(destinationAccountNumber);
    }
}
