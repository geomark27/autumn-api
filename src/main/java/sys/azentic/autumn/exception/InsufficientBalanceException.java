package sys.azentic.autumn.exception;

import java.math.BigDecimal;

/**
 * Excepción lanzada cuando una cuenta no tiene saldo suficiente.
 */
public class InsufficientBalanceException extends RuntimeException {
    
    public InsufficientBalanceException(String accountNumber, BigDecimal required, BigDecimal available) {
        super(String.format("Saldo insuficiente en cuenta %s. Requerido: %.2f, Disponible: %.2f",
            accountNumber, required, available));
    }
}
