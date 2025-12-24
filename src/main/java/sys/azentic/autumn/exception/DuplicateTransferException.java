package sys.azentic.autumn.exception;

/**
 * Excepción lanzada cuando se intenta realizar una transferencia duplicada.
 */
public class DuplicateTransferException extends RuntimeException {
    
    public DuplicateTransferException(String idempotencyKey) {
        super("Ya existe una transferencia con la clave de idempotencia: " + idempotencyKey);
    }
}
