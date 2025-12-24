package sys.azentic.autumn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sys.azentic.autumn.domain.entity.AuditEvent;
import sys.azentic.autumn.domain.enums.AuditEventType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad AuditEvent.
 * IMPORTANTE: Solo operaciones de INSERT y SELECT. Nunca UPDATE o DELETE.
 */
@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

    /**
     * Busca todos los eventos de auditoría para un agregado específico (ej: una transferencia).
     */
    @Query("SELECT a FROM AuditEvent a WHERE a.aggregateId = :aggregateId ORDER BY a.createdAt ASC")
    List<AuditEvent> findByAggregateIdOrderByCreatedAtAsc(@Param("aggregateId") UUID aggregateId);

    /**
     * Busca eventos por tipo.
     */
    List<AuditEvent> findByEventType(AuditEventType eventType);

    /**
     * Obtiene el último evento de auditoría para validación de hash chain.
     * Necesario para calcular el hash del próximo evento.
     */
    @Query("SELECT a FROM AuditEvent a ORDER BY a.createdAt DESC LIMIT 1")
    Optional<AuditEvent> findLatestEvent();

    /**
     * Obtiene el último evento para un agregado específico.
     */
    @Query("SELECT a FROM AuditEvent a WHERE a.aggregateId = :aggregateId ORDER BY a.createdAt DESC LIMIT 1")
    Optional<AuditEvent> findLatestEventByAggregateId(@Param("aggregateId") UUID aggregateId);

    /**
     * Verifica la integridad de la cadena de hash.
     * Retorna eventos donde el hash no coincide con el calculado.
     */
    @Query("SELECT a FROM AuditEvent a WHERE a.previousHash IS NOT NULL")
    List<AuditEvent> findAllForHashValidation();
}
