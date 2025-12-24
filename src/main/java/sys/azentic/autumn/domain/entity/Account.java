package sys.azentic.autumn.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sys.azentic.autumn.domain.enums.AccountStatus;
import sys.azentic.autumn.domain.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa una cuenta bancaria.
 * Incluye control de concurrencia optimista mediante @Version para prevenir
 * actualizaciones concurrentes del saldo.
 */
@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_account_number", columnList = "account_number", unique = true),
    @Index(name = "idx_account_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    /**
     * Control de concurrencia optimista.
     * Se incrementa automáticamente en cada actualización.
     */
    @Version
    private Long version;

    @Column(nullable = false)
    private String ownerName;

    @Column(length = 100)
    private String ownerEmail;

    /**
     * Límite diario de transferencias salientes
     */
    @Column(precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal dailyLimit = new BigDecimal("50000.00");

    /**
     * Acumulado de transferencias realizadas hoy
     */
    @Column(precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal dailyUsed = BigDecimal.ZERO;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Verifica si la cuenta puede realizar una transferencia.
     */
    public boolean canTransfer(BigDecimal amount) {
        return status == AccountStatus.ACTIVE 
            && balance.compareTo(amount) >= 0
            && dailyUsed.add(amount).compareTo(dailyLimit) <= 0;
    }

    /**
     * Verifica si la cuenta puede recibir transferencias.
     */
    public boolean canReceive() {
        return status == AccountStatus.ACTIVE;
    }

    /**
     * Debita un monto de la cuenta.
     * @throws IllegalStateException si el saldo es insuficiente
     */
    public void debit(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Saldo insuficiente");
        }
        this.balance = this.balance.subtract(amount);
        this.dailyUsed = this.dailyUsed.add(amount);
    }

    /**
     * Acredita un monto a la cuenta.
     */
    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    /**
     * Resetea el uso diario (ejecutado por proceso nocturno).
     */
    public void resetDailyUsage() {
        this.dailyUsed = BigDecimal.ZERO;
    }
}
