package sys.azentic.autumn.domain.enums;

/**
 * Tipos de eventos de auditoría para Event Sourcing.
 */
public enum AuditEventType {
    TRANSFER_CREATED,
    TRANSFER_VALIDATED,
    TRANSFER_PROCESSING,
    TRANSFER_COMPLETED,
    TRANSFER_FAILED,
    TRANSFER_COMPENSATED,
    ACCOUNT_DEBITED,
    ACCOUNT_CREDITED,
    IDEMPOTENCY_CHECK
}
