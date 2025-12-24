package sys.azentic.autumn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sys.azentic.autumn.domain.entity.LedgerEntry;

import java.util.List;
import java.util.UUID;

/**
 * Repositorio para la entidad LedgerEntry.
 * IMPORTANTE: Solo operaciones de INSERT y SELECT. Nunca UPDATE o DELETE.
 */
@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    /**
     * Busca todos los asientos del libro mayor para una transferencia específica.
     */
    List<LedgerEntry> findByTransferId(UUID transferId);

    /**
     * Busca todos los asientos del libro mayor para una cuenta específica.
     */
    @Query("SELECT l FROM LedgerEntry l WHERE l.account.id = :accountId ORDER BY l.createdAt DESC")
    List<LedgerEntry> findByAccountId(@Param("accountId") UUID accountId);

    /**
     * Busca los últimos N asientos de una cuenta para auditoría.
     */
    @Query("SELECT l FROM LedgerEntry l WHERE l.account.id = :accountId ORDER BY l.createdAt DESC LIMIT :limit")
    List<LedgerEntry> findRecentByAccountId(@Param("accountId") UUID accountId, @Param("limit") int limit);
}
