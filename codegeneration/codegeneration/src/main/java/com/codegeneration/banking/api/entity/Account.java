package com.codegeneration.banking.api.entity;

import com.codegeneration.banking.api.enums.Currency;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "accounts",
        indexes = {
                @Index(name = "idx_account_number", columnList = "accountNumber"),
                @Index(name = "idx_account_user", columnList = "user_id"),
                @Index(name = "idx_account_type", columnList = "accountType"),
                @Index(name = "idx_account_currency", columnList = "currency"),
                @Index(name = "idx_account_created", columnList = "createdAt")
        }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private String accountName;

    @Column(nullable = false)
    private String accountType;    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;    @Column(nullable = false)
    @Builder.Default
    private Currency currency = Currency.EUR; // Default to Euro as standardized currency    // Daily limits
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal dailyTransferLimit = new BigDecimal("5000.00");    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal dailyWithdrawalLimit = new BigDecimal("5000.00");    // Per-transaction limits
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal singleTransferLimit = new BigDecimal("3000.00");    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal singleWithdrawalLimit = new BigDecimal("500.00");    // Current period usage tracking
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal transferUsedToday = BigDecimal.ZERO;    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal withdrawalUsedToday = BigDecimal.ZERO;    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime lastLimitResetDate = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference   // This pairs with @JsonManagedReference on User.accounts
    private User user;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;


    //for tests
    public Account(long l, String testAccountNumber, BigDecimal bigDecimal) {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Resets the daily usage counters if a new day has started
     */
    public void resetDailyLimitsIfNeeded() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime resetDate = lastLimitResetDate.toLocalDate().atStartOfDay().plusDays(1);

        if (now.isAfter(resetDate)) {
            transferUsedToday = BigDecimal.ZERO;
            withdrawalUsedToday = BigDecimal.ZERO;
            lastLimitResetDate = now;
        }
    }

    /**
     * Checks if a transfer amount is within limits
     *
     * @param amount Transfer amount
     * @return true if transfer is allowed, false otherwise
     */
    public boolean isTransferAllowed(BigDecimal amount) {
        resetDailyLimitsIfNeeded();

        if (amount.compareTo(singleTransferLimit) > 0) {
            return false;
        }

        BigDecimal newTotal = transferUsedToday.add(amount);
        return newTotal.compareTo(dailyTransferLimit) <= 0;
    }

    /**
     * Checks if a withdrawal amount is within limits
     *
     * @param amount Withdrawal amount
     * @return true if withdrawal is allowed, false otherwise
     */
    public boolean isWithdrawalAllowed(BigDecimal amount) {
        resetDailyLimitsIfNeeded();

        if (amount.compareTo(singleWithdrawalLimit) > 0) {
            return false;
        }

        BigDecimal newTotal = withdrawalUsedToday.add(amount);
        return newTotal.compareTo(dailyWithdrawalLimit) <= 0;
    }

    /**
     * Updates the transfer used amount for today
     *
     * @param amount Transfer amount
     */
    public void updateTransferUsed(BigDecimal amount) {
        resetDailyLimitsIfNeeded();
        transferUsedToday = transferUsedToday.add(amount);
    }

    /**
     * Updates the withdrawal used amount for today
     *
     * @param amount Withdrawal amount
     */
    public void updateWithdrawalUsed(BigDecimal amount) {
        resetDailyLimitsIfNeeded();
        withdrawalUsedToday = withdrawalUsedToday.add(amount);
    }
}