package sys.azentic.autumn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sys.azentic.autumn.domain.entity.Transfer;
import sys.azentic.autumn.domain.enums.TransferStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad Transfer.
 */
@Repository
public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    /**
     * Busca una transferencia por su clave de idempotencia.
     * Fundamental para prevenir duplicados.
     */
    Optional<Transfer> findByIdempotencyKey(UUID idempotencyKey);

    /**
     * Verifica si existe una transferencia con la clave de idempotencia dada.
     */
    boolean existsByIdempotencyKey(UUID idempotencyKey);

    /**
     * Busca transferencias por cuenta de origen.
     */
    @Query("SELECT t FROM Transfer t WHERE t.sourceAccount.id = :accountId ORDER BY t.createdAt DESC")
    List<Transfer> findBySourceAccountId(@Param("accountId") UUID accountId);

    /**
     * Busca transferencias por cuenta de destino.
     */
    @Query("SELECT t FROM Transfer t WHERE t.destinationAccount.id = :accountId ORDER BY t.createdAt DESC")
    List<Transfer> findByDestinationAccountId(@Param("accountId") UUID accountId);

    /**
     * Busca todas las transferencias de una cuenta (origen o destino).
     */
    @Query("SELECT t FROM Transfer t WHERE t.sourceAccount.id = :accountId OR t.destinationAccount.id = :accountId ORDER BY t.createdAt DESC")
    List<Transfer> findByAccountId(@Param("accountId") UUID accountId);

    /**
     * Busca transferencias pendientes de aprobación.
     */
    List<Transfer> findByRequiresApprovalTrueAndStatus(TransferStatus status);

    /**
     * Busca transferencias por estado y rango de fechas.
     */
    @Query("SELECT t FROM Transfer t WHERE t.status = :status AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Transfer> findByStatusAndDateRange(
        @Param("status") TransferStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Cuenta transferencias completadas en un rango de fechas para una cuenta.
     * Útil para estadísticas y límites.
     */
    @Query("SELECT COUNT(t) FROM Transfer t WHERE t.sourceAccount.id = :accountId AND t.status = 'COMPLETED' AND t.createdAt BETWEEN :startDate AND :endDate")
    Long countCompletedTransfersByAccountAndDateRange(
        @Param("accountId") UUID accountId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
