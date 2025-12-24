package sys.azentic.autumn.exception;

/**
 * Excepción lanzada cuando una cuenta está inactiva o bloqueada.
 */
public class InactiveAccountException extends RuntimeException {
    
    public InactiveAccountException(String accountNumber, String status) {
        super(String.format("La cuenta %s está %s y no puede realizar operaciones", accountNumber, status));
    }
}
