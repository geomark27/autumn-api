package sys.azentic.autumn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sys.azentic.autumn.domain.enums.AccountStatus;
import sys.azentic.autumn.domain.enums.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de respuesta para información de cuenta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponse {

    private UUID id;
    private String accountNumber;
    private BigDecimal balance;
    private Currency currency;
    private AccountStatus status;
    private String ownerName;
    private BigDecimal dailyLimit;
    private BigDecimal dailyUsed;
    private LocalDateTime createdAt;
}
