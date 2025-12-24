package sys.azentic.autumn.exception;

import java.util.UUID;

/**
 * Excepción lanzada cuando no se encuentra una cuenta.
 */
public class AccountNotFoundException extends RuntimeException {
    
    public AccountNotFoundException(String accountNumber) {
        super("Cuenta no encontrada: " + accountNumber);
    }
    
    public AccountNotFoundException(UUID accountId) {
        super("Cuenta no encontrada con ID: " + accountId);
    }
}
