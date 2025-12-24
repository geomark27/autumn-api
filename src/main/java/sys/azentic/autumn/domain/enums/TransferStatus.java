package sys.azentic.autumn.domain.enums;

/**
 * Estados posibles de una transferencia bancaria.
 * Sigue el patrón de máquina de estados para trazabilidad completa.
 */
public enum TransferStatus {
    /**
     * Solicitud recibida, esperando procesamiento
     */
    PENDING,
    
    /**
     * Validaciones aprobadas, ejecución en curso
     */
    PROCESSING,
    
    /**
     * Transferencia completada exitosamente
     */
    COMPLETED,
    
    /**
     * Error detectado (validación de negocio o técnico)
     */
    FAILED,
    
    /**
     * Transferencia revertida automáticamente por fallo en el flujo (Saga Pattern)
     */
    COMPENSATED
}
