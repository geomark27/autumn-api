package sys.azentic.autumn.domain.enums;

/**
 * Tipos de movimientos en el libro mayor (Double-entry bookkeeping).
 */
public enum LedgerEntryType {
    /**
     * Débito - resta de saldo (cuenta origen)
     */
    DEBIT,
    
    /**
     * Crédito - suma de saldo (cuenta destino)
     */
    CREDIT
}
