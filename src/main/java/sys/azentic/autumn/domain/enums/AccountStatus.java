package sys.azentic.autumn.domain.enums;

/**
 * Estado de una cuenta bancaria.
 */
public enum AccountStatus {
    /**
     * Cuenta activa, puede realizar y recibir transferencias
     */
    ACTIVE,
    
    /**
     * Cuenta bloqueada temporalmente por seguridad
     */
    BLOCKED,
    
    /**
     * Cuenta cerrada permanentemente
     */
    CLOSED
}
