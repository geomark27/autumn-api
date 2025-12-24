package sys.azentic.autumn.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import sys.azentic.autumn.domain.enums.LedgerEntryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad INMUTABLE que representa un asiento en el libro mayor.
 * Implementa Double-entry bookkeeping: cada transferencia genera dos entradas (DEBIT + CREDIT).
 * 
 * NUNCA debe modificarse después de crearse. Solo INSERT, nunca UPDATE o DELETE.
 */
@Entity
@Table(name = "ledger_entries", indexes = {
    @Index(name = "idx_ledger_transfer_id", columnList = "transfer_id"),
    @Index(name = "idx_ledger_account_id", columnList = "account_id"),
    @Index(name = "idx_ledger_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transfer_id", nullable = false)
    private Transfer transfer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LedgerEntryType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    /**
     * Saldo de la cuenta DESPUÉS de aplicar este movimiento.
     * Fundamental para reconciliación y auditoría.
     */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @Column(length = 500)
    private String description;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Factory method para crear un débito
     */
    public static LedgerEntry createDebit(Transfer transfer, Account account, BigDecimal amount, String description) {
        return LedgerEntry.builder()
            .transfer(transfer)
            .account(account)
            .type(LedgerEntryType.DEBIT)
            .amount(amount)
            .balanceAfter(account.getBalance()) // Captura el saldo DESPUÉS del débito
            .description(description)
            .build();
    }

    /**
     * Factory method para crear un crédito
     */
    public static LedgerEntry createCredit(Transfer transfer, Account account, BigDecimal amount, String description) {
        return LedgerEntry.builder()
            .transfer(transfer)
            .account(account)
            .type(LedgerEntryType.CREDIT)
            .amount(amount)
            .balanceAfter(account.getBalance()) // Captura el saldo DESPUÉS del crédito
            .description(description)
            .build();
    }
}
