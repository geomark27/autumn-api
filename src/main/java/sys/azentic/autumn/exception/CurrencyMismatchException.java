package sys.azentic.autumn.exception;

/**
 * Excepción lanzada cuando se intenta transferir entre cuentas de diferentes monedas.
 */
public class CurrencyMismatchException extends RuntimeException {
    
    public CurrencyMismatchException(String sourceCurrency, String destinationCurrency) {
        super(String.format("Las cuentas deben tener la misma moneda. Origen: %s, Destino: %s",
            sourceCurrency, destinationCurrency));
    }
}
