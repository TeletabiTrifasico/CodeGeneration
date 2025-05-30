package com.codegeneration.banking.api.service.implementations;

import com.codegeneration.banking.api.dto.transactionfilter.TransactionFilterRequest;
import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.Transaction;
import com.codegeneration.banking.api.service.interfaces.AccountService;
import com.codegeneration.banking.api.service.interfaces.TransactionFilterService;
import com.codegeneration.banking.api.service.interfaces.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionFilterServiceImpl implements TransactionFilterService {

    private final TransactionService transactionService;
    private final AccountService accountService;

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> filterTransactions(List<Transaction> transactions, TransactionFilterRequest filterRequest) {
        if (filterRequest == null) {
            return transactions;
        }

        return transactions.stream()
                .filter(transaction -> matchesDateFilter(transaction, filterRequest))
                .filter(transaction -> matchesAmountFilter(transaction, filterRequest))
                .filter(transaction -> matchesIbanFilter(transaction, filterRequest))
                .filter(transaction -> matchesTypeFilter(transaction, filterRequest))
                .filter(transaction -> matchesStatusFilter(transaction, filterRequest))
                .filter(transaction -> matchesDescriptionFilter(transaction, filterRequest))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getFilteredTransactionsByUsername(String username, TransactionFilterRequest filterRequest) {
        List<Transaction> allTransactions = transactionService.getAllTransactionsByUsername(username);
        return filterTransactions(allTransactions, filterRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getFilteredTransactionsByAccount(String accountNumber, String username, TransactionFilterRequest filterRequest) {
        Account account = accountService.getAccountByNumberAndUsername(accountNumber, username);
        if (account == null) {
            return List.of();
        }

        List<Transaction> accountTransactions = transactionService.getTransactionsByAccount(account);
        return filterTransactions(accountTransactions, filterRequest);
    }

    private boolean matchesDateFilter(Transaction transaction, TransactionFilterRequest filterRequest) {
        LocalDate transactionDate = transaction.getCreatedAt().toLocalDate();

        if (filterRequest.getStartDate() != null) {
            if (transactionDate.isBefore(filterRequest.getStartDate())) {
                return false;
            }
        }

        if (filterRequest.getEndDate() != null) {
            if (transactionDate.isAfter(filterRequest.getEndDate())) {
                return false;
            }
        }

        return true;
    }

    private boolean matchesAmountFilter(Transaction transaction, TransactionFilterRequest filterRequest) {
        BigDecimal amount = transaction.getAmount();

        if (filterRequest.getAmountEqualTo() != null) {
            return amount.compareTo(filterRequest.getAmountEqualTo()) == 0;
        }

        if (filterRequest.getAmountLessThan() != null) {
            if (amount.compareTo(filterRequest.getAmountLessThan()) >= 0) {
                return false;
            }
        }

        if (filterRequest.getAmountGreaterThan() != null) {
            if (amount.compareTo(filterRequest.getAmountGreaterThan()) <= 0) {
                return false;
            }
        }

        return true;
    }

    private boolean matchesIbanFilter(Transaction transaction, TransactionFilterRequest filterRequest) {
        if (filterRequest.getIban() == null || filterRequest.getIban().trim().isEmpty()) {
            return true;
        }

        String searchIban = filterRequest.getIban().trim().toLowerCase();
        String sourceAccountNumber = transaction.getSourceAccount().getAccountNumber().toLowerCase();
        String destinationAccountNumber = transaction.getDestinationAccount().getAccountNumber().toLowerCase();

        return sourceAccountNumber.contains(searchIban) || destinationAccountNumber.contains(searchIban);
    }

    private boolean matchesTypeFilter(Transaction transaction, TransactionFilterRequest filterRequest) {
        if (filterRequest.getTransactionType() == null || filterRequest.getTransactionType().trim().isEmpty()) {
            return true;
        }

        return transaction.getType().name().equalsIgnoreCase(filterRequest.getTransactionType().trim());
    }

    private boolean matchesStatusFilter(Transaction transaction, TransactionFilterRequest filterRequest) {
        if (filterRequest.getTransactionStatus() == null || filterRequest.getTransactionStatus().trim().isEmpty()) {
            return true;
        }

        return transaction.getStatus().name().equalsIgnoreCase(filterRequest.getTransactionStatus().trim());
    }

    private boolean matchesDescriptionFilter(Transaction transaction, TransactionFilterRequest filterRequest) {
        if (filterRequest.getDescription() == null || filterRequest.getDescription().trim().isEmpty()) {
            return true;
        }

        String description = transaction.getDescription();
        if (description == null) {
            return false;
        }

        return description.toLowerCase().contains(filterRequest.getDescription().trim().toLowerCase());
    }
}