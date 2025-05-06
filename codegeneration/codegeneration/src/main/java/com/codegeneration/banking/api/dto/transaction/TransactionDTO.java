package com.codegeneration.banking.api.dto.transaction;

import com.codegeneration.banking.api.entity.Transaction;
import com.codegeneration.banking.api.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private String transactionReference;
    private AccountDTO sourceAccount;
    private AccountDTO destinationAccount;
    private BigDecimal amount;
    private Currency currency;
    private String description;
    private String transactionStatus;
    private String transactionType;
    private String createAt;
    private String completedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountDTO {
        private Long id;
        private String accountNumber;
        private String accountName;
        private String accountType;
    }

    public static TransactionDTO fromEntity(Transaction transaction) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return TransactionDTO.builder()
                .id(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .sourceAccount(AccountDTO.builder()
                        .id(transaction.getSourceAccount().getId())
                        .accountNumber(transaction.getSourceAccount().getAccountNumber())
                        .accountName(transaction.getSourceAccount().getAccountName())
                        .accountType(transaction.getSourceAccount().getAccountType())
                        .build())
                .destinationAccount(AccountDTO.builder()
                        .id(transaction.getDestinationAccount().getId())
                        .accountNumber(transaction.getDestinationAccount().getAccountNumber())
                        .accountName(transaction.getDestinationAccount().getAccountName())
                        .accountType(transaction.getDestinationAccount().getAccountType())
                        .build())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .transactionStatus(transaction.getStatus().name())
                .transactionType(transaction.getType().name())
                .createAt(transaction.getCreatedAt().format(formatter))
                .completedAt(transaction.getCompletedAt() != null
                        ? transaction.getCompletedAt().format(formatter)
                        : null)
                .build();
    }
}