package com.codegeneration.banking.api.config;

import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.Transaction;
import com.codegeneration.banking.api.entity.User;
import com.codegeneration.banking.api.enums.Currency;
import com.codegeneration.banking.api.enums.UserRole;
import com.codegeneration.banking.api.repository.AccountRepository;
import com.codegeneration.banking.api.repository.TransactionRepository;
import com.codegeneration.banking.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Check if users already exist
        if (userRepository.count() == 0) {
            loadUsers();
            loadAccounts();
            loadTransactions();
        }
    }

    private void loadUsers() {
        log.info("Loading sample users...");

        // Employee user
        User employee = User.builder()
                .username("employee")
                .password(passwordEncoder.encode("employee123"))
                .name("Bank Employee")
                .email("employee@bankapp.com")
                .role(UserRole.EMPLOYEE)
                .enabled(true)
                .build();

        // Client users
        User user1 = User.builder()
                .username("user1")
                .password(passwordEncoder.encode("user123"))
                .name("Jan Kowalski")
                .email("jan.kowalski@example.com")
                .role(UserRole.CLIENT)
                .enabled(true)
                .build();
        User user2 = User.builder()
                .username("user2")
                .password(passwordEncoder.encode("user123"))
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .role(UserRole.CLIENT)
                .enabled(true)
                .build();

        // Disabled users pending approval
        User pendingUser1 = User.builder()
                .username("pending1")
                .password(passwordEncoder.encode("pending123"))
                .name("Alice Johnson")
                .email("alice.johnson@example.com")
                .role(UserRole.CLIENT)
                .enabled(false)
                .build();

        User pendingUser2 = User.builder()
                .username("pending2")
                .password(passwordEncoder.encode("pending123"))
                .name("Bob Wilson")
                .email("bob.wilson@example.com")
                .role(UserRole.CLIENT)
                .enabled(false)
                .build();

        User pendingUser3 = User.builder()
                .username("pending3")
                .password(passwordEncoder.encode("pending123"))
                .name("Carol Davis")
                .email("carol.davis@example.com")
                .role(UserRole.CLIENT)
                .enabled(false)
                .build();

        userRepository.saveAll(Arrays.asList(employee, user1, user2, pendingUser1, pendingUser2, pendingUser3));

        log.info("Sample users loaded successfully");
    }

    private void loadAccounts() {
        log.info("Loading sample accounts...");

        // Get users
        User user1 = userRepository.findByUsername("user1").orElseThrow();
        User user2 = userRepository.findByUsername("user2").orElseThrow();

        LocalDateTime now = LocalDateTime.now();

        // Create accounts for user1 (EUR and PLN)
        Account user1EurAccount = Account.builder()
                .accountNumber("NL99BANK012345679")
                .accountName("Jan's Euro Account")
                .accountType("CHECKING")
                .balance(new BigDecimal("5000.00"))
                .currency(Currency.EUR)
                .dailyTransferLimit(new BigDecimal("1000.00"))
                .dailyWithdrawalLimit(new BigDecimal("2000.00"))
                .singleTransferLimit(new BigDecimal("5000.00"))
                .singleWithdrawalLimit(new BigDecimal("1000.00"))
                .transferUsedToday(BigDecimal.ZERO)
                .withdrawalUsedToday(BigDecimal.ZERO)
                .lastLimitResetDate(now)
                .user(user1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Account user1PlnAccount = Account.builder()
                .accountNumber("NL99BANK014345619")
                .accountName("Jan's PLN Account")
                .accountType("SAVINGS")
                .balance(new BigDecimal("10000.00"))
                .currency(Currency.PLN)
                .dailyTransferLimit(new BigDecimal("20000.00"))
                .dailyWithdrawalLimit(new BigDecimal("5000.00"))
                .singleTransferLimit(new BigDecimal("10000.00"))
                .singleWithdrawalLimit(new BigDecimal("2000.00"))
                .transferUsedToday(BigDecimal.ZERO)
                .withdrawalUsedToday(BigDecimal.ZERO)
                .lastLimitResetDate(now)
                .user(user1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Create account for user2 (EUR only)
        Account user2EurAccount = Account.builder()
                .accountNumber("NL99BANK823345941")
                .accountName("Jane's Euro Account")
                .accountType("CHECKING")
                .balance(new BigDecimal("3000.00"))
                .currency(Currency.EUR)
                .dailyTransferLimit(new BigDecimal("5000.00"))
                .dailyWithdrawalLimit(new BigDecimal("1000.00"))
                .singleTransferLimit(new BigDecimal("3000.00"))
                .singleWithdrawalLimit(new BigDecimal("500.00"))
                .transferUsedToday(BigDecimal.ZERO)
                .withdrawalUsedToday(BigDecimal.ZERO)
                .lastLimitResetDate(now)
                .user(user2)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Save accounts
        accountRepository.saveAll(Arrays.asList(user1EurAccount, user1PlnAccount, user2EurAccount));

        log.info("Sample accounts loaded successfully");
    }

    private void loadTransactions() {
        log.info("Loading sample transactions...");

        // Get accounts
        Account user1EurAccount = accountRepository.findByAccountNumber("NL99BANK012345679").orElseThrow();
        Account user1PlnAccount = accountRepository.findByAccountNumber("NL99BANK014345619").orElseThrow();
        Account user2EurAccount = accountRepository.findByAccountNumber("NL99BANK823345941").orElseThrow();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime lastWeek = now.minusWeeks(1);

        // Create transactions between user1's EUR account and user2's EUR account
        Transaction transaction1 = Transaction.builder()
                .transactionReference(generateTransactionReference())
                .sourceAccount(user1EurAccount)
                .destinationAccount(user2EurAccount)
                .amount(new BigDecimal("100.00"))
                .currency(Currency.EUR)
                .description("Payment for dinner")
                .status(Transaction.TransactionStatus.COMPLETED)
                .type(Transaction.TransactionType.TRANSFER)
                .createdAt(yesterday)
                .completedAt(yesterday.plusMinutes(2))
                .build();

        Transaction transaction2 = Transaction.builder()
                .transactionReference(generateTransactionReference())
                .sourceAccount(user2EurAccount)
                .destinationAccount(user1EurAccount)
                .amount(new BigDecimal("50.00"))
                .currency(Currency.EUR)
                .description("Splitting the cab fare")
                .status(Transaction.TransactionStatus.COMPLETED)
                .type(Transaction.TransactionType.TRANSFER)
                .createdAt(yesterday.plusHours(5))
                .completedAt(yesterday.plusHours(5).plusMinutes(1))
                .build();

        // Create transaction between user1's two accounts (internal transfer)
        Transaction transaction3 = Transaction.builder()
                .transactionReference(generateTransactionReference())
                .sourceAccount(user1EurAccount)
                .destinationAccount(user1PlnAccount)
                .amount(new BigDecimal("200.00"))
                .currency(Currency.EUR)
                .description("Transfer to PLN account")
                .status(Transaction.TransactionStatus.COMPLETED)
                .type(Transaction.TransactionType.TRANSFER)
                .createdAt(lastWeek)
                .completedAt(lastWeek.plusMinutes(1))
                .build();

        // Create deposit transaction
        Transaction transaction4 = Transaction.builder()
                .transactionReference(generateTransactionReference())
                .destinationAccount(user1EurAccount)
                .sourceAccount(user1EurAccount) // Self reference for deposit
                .amount(new BigDecimal("1000.00"))
                .currency(Currency.EUR)
                .description("Salary deposit")
                .status(Transaction.TransactionStatus.COMPLETED)
                .type(Transaction.TransactionType.ATM_DEPOSIT)
                .createdAt(lastWeek.plusDays(2))
                .completedAt(lastWeek.plusDays(2).plusMinutes(5))
                .build();

        // Create withdrawal transaction
        Transaction transaction5 = Transaction.builder()
                .transactionReference(generateTransactionReference())
                .sourceAccount(user2EurAccount)
                .destinationAccount(user2EurAccount) // Self reference for withdrawal
                .amount(new BigDecimal("200.00"))
                .currency(Currency.EUR)
                .description("ATM Withdrawal")
                .status(Transaction.TransactionStatus.COMPLETED)
                .type(Transaction.TransactionType.ATM_WITHDRAWAL)
                .createdAt(yesterday.minusDays(1))
                .completedAt(yesterday.minusDays(1).plusSeconds(30))
                .build();

        // Save transactions
        transactionRepository.saveAll(Arrays.asList(
                transaction1, transaction2, transaction3, transaction4, transaction5
        ));

        log.info("Sample transactions loaded successfully");
    }

    /**
     * Generate a unique transaction reference
     * @return Transaction reference string
     */
    private String generateTransactionReference() {
        return "TRX" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}