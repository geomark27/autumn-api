package sys.azentic.autumn.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sys.azentic.autumn.domain.enums.AuditEventType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad INMUTABLE para Event Sourcing con Hash Chaining.
 * Cada evento genera un hash que incluye el hash del evento anterior,
 * creando una cadena de confianza para detectar manipulaciones.
 * 
 * NUNCA debe modificarse después de crearse. Solo INSERT.
 */
@Entity
@Table(name = "audit_events", indexes = {
    @Index(name = "idx_audit_aggregate_id", columnList = "aggregate_id"),
    @Index(name = "idx_audit_event_type", columnList = "event_type"),
    @Index(name = "idx_audit_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ID de la entidad auditada (por ejemplo, el ID de la transferencia)
     */
    @Column(nullable = false)
    private UUID aggregateId;

    /**
     * Tipo de agregado auditado (Transfer, Account, etc.)
     */
    @Column(nullable = false, length = 50)
    private String aggregateType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AuditEventType eventType;

    /**
     * Datos del evento en formato JSON
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    /**
     * Hash SHA-256 del evento actual.
     * Se calcula como: SHA256(previousHash + aggregateId + eventType + payload + timestamp)
     */
    @Column(nullable = false, length = 64)
    private String eventHash;

    /**
     * Hash del evento inmediatamente anterior en la cadena.
     * El primer evento de la cadena tiene previousHash = "0" o null.
     */
    @Column(length = 64)
    private String previousHash;

    /**
     * Usuario que ejecutó la acción (puede ser null para eventos del sistema)
     */
    private UUID userId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Metadata adicional (IP, User-Agent, etc.)
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;
}
