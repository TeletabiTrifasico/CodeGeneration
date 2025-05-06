package com.codegeneration.banking.api.dto.account;

import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private Long id;
    private String accountNumber;
    private String accountName;
    private String accountType;
    private BigDecimal balance;
    private Currency currency;
    private BigDecimal dailyTransferLimit;
    private BigDecimal dailyWithdrawalLimit;
    private BigDecimal singleTransferLimit;
    private BigDecimal singleWithdrawalLimit;
    private BigDecimal transferUsedToday;
    private BigDecimal withdrawalUsedToday;
    private String lastLimitResetDate;
    private String createdAt;
    private String updatedAt;

    public static AccountDTO fromEntity(Account account) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return AccountDTO.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .dailyTransferLimit(account.getDailyTransferLimit())
                .dailyWithdrawalLimit(account.getDailyWithdrawalLimit())
                .singleTransferLimit(account.getSingleTransferLimit())
                .singleWithdrawalLimit(account.getSingleWithdrawalLimit())
                .transferUsedToday(account.getTransferUsedToday())
                .withdrawalUsedToday(account.getWithdrawalUsedToday())
                .lastLimitResetDate(account.getLastLimitResetDate().format(formatter))
                .createdAt(account.getCreatedAt().format(formatter))
                .updatedAt(account.getUpdatedAt().format(formatter))
                .build();
    }
}