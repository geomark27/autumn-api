package sys.azentic.autumn.exception;

import java.math.BigDecimal;

/**
 * Excepción lanzada cuando se excede el límite diario de transferencias.
 */
public class DailyLimitExceededException extends RuntimeException {
    
    public DailyLimitExceededException(String accountNumber, BigDecimal limit, BigDecimal used, BigDecimal requested) {
        super(String.format("Límite diario excedido para cuenta %s. Límite: %.2f, Usado: %.2f, Solicitado: %.2f",
            accountNumber, limit, used, requested));
    }
}
