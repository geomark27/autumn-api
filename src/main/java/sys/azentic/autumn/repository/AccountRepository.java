package sys.azentic.autumn.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sys.azentic.autumn.domain.entity.Account;
import sys.azentic.autumn.domain.enums.AccountStatus;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad Account.
 * Incluye métodos con bloqueo pesimista para evitar race conditions en transferencias.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    /**
     * Busca una cuenta por su número.
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Busca una cuenta por su número con bloqueo pesimista.
     * Este método debe usarse dentro de una transacción para bloquear la fila
     * y evitar que otras transacciones la modifiquen simultáneamente.
     * 
     * CRITICAL: Solo usar dentro de @Transactional
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberWithLock(@Param("accountNumber") String accountNumber);

    /**
     * Verifica si existe una cuenta activa con el número dado.
     */
    boolean existsByAccountNumberAndStatus(String accountNumber, AccountStatus status);

    /**
     * Busca todas las cuentas de un propietario.
     */
    @Query("SELECT a FROM Account a WHERE a.ownerEmail = :email")
    java.util.List<Account> findByOwnerEmail(@Param("email") String email);
}
