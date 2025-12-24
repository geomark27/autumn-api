package sys.azentic.autumn.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sys.azentic.autumn.domain.enums.TransferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa una transferencia bancaria.
 * Mantiene el estado completo de la transacción para trazabilidad.
 */
@Entity
@Table(name = "transfers", indexes = {
    @Index(name = "idx_transfer_idempotency_key", columnList = "idempotency_key", unique = true),
    @Index(name = "idx_transfer_status", columnList = "status"),
    @Index(name = "idx_transfer_source_account", columnList = "source_account_id"),
    @Index(name = "idx_transfer_destination_account", columnList = "destination_account_id"),
    @Index(name = "idx_transfer_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Clave única para garantizar idempotencia.
     * El cliente debe enviar este UUID en cada request.
     */
    @Column(nullable = false, unique = true)
    private UUID idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_account_id", nullable = false)
    private Account destinationAccount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TransferStatus status = TransferStatus.PENDING;

    @Column(length = 500)
    private String description;

    /**
     * Mensaje de error si el estado es FAILED o COMPENSATED
     */
    @Column(length = 1000)
    private String errorMessage;

    /**
     * Requiere aprobación manual si el monto supera el límite configurado
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean requiresApproval = false;

    /**
     * ID del usuario que aprobó (si requiresApproval = true)
     */
    private UUID approvedBy;

    private LocalDateTime approvedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Timestamp de cuando se completó la transferencia
     */
    private LocalDateTime completedAt;

    /**
     * Marca la transferencia como completada
     */
    public void markAsCompleted() {
        this.status = TransferStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Marca la transferencia como fallida
     */
    public void markAsFailed(String error) {
        this.status = TransferStatus.FAILED;
        this.errorMessage = error;
    }

    /**
     * Marca la transferencia como compensada (revertida)
     */
    public void markAsCompensated(String reason) {
        this.status = TransferStatus.COMPENSATED;
        this.errorMessage = reason;
    }

    /**
     * Inicia el procesamiento
     */
    public void startProcessing() {
        this.status = TransferStatus.PROCESSING;
    }

    /**
     * Verifica si la transferencia está en un estado final
     */
    public boolean isFinalState() {
        return status == TransferStatus.COMPLETED 
            || status == TransferStatus.FAILED 
            || status == TransferStatus.COMPENSATED;
    }
}
