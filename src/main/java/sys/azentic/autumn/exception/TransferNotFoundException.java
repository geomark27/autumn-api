package sys.azentic.autumn.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando no se encuentra una transferencia.
 */
public class TransferNotFoundException extends RuntimeException {
    
    public TransferNotFoundException(UUID transferId) {
        super("Transferencia no encontrada con ID: " + transferId);
    }
}
