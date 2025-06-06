package com.codegeneration.banking.api.service.implementations;

import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.Transaction;
import com.codegeneration.banking.api.entity.Transaction.TransactionType;
import com.codegeneration.banking.api.repository.AccountRepository;
import com.codegeneration.banking.api.repository.TransactionRepository;
import com.codegeneration.banking.api.service.interfaces.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactionsByUsername(String username) {

        List<Account> userAccounts = accountRepository.findByUserUsername(username);

        List<Transaction> allTransactions = new ArrayList<>();
        for (Account account : userAccounts) {

            allTransactions.addAll(transactionRepository.findBySourceAccountOrDestinationAccount(account, account));
        }

        return allTransactions;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByAccount(Account account) {

        return transactionRepository.findBySourceAccountOrDestinationAccount(account, account);
    }

    @Override
    @Transactional
    public Transaction createAtmTransaction(Account account, double amount, TransactionType type, String description) {
        // Validate transaction type
        if (type != TransactionType.ATM_DEPOSIT && type != TransactionType.ATM_WITHDRAWAL) {
            throw new IllegalArgumentException("Invalid transaction type for ATM transaction: " + type);
        }
        
        // Validate amount
        if (amount <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(amount));
        transaction.setType(type);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setCompletedAt(LocalDateTime.now());
        transaction.setCurrency(account.getCurrency());
        
        // Generate a unique transaction reference
        String transactionReference = generateTransactionReference(type);
        transaction.setTransactionReference(transactionReference);
        
        // For ATM transactions, both source and destination are the same account
        transaction.setSourceAccount(account);
        transaction.setDestinationAccount(account);
        
        // Set description
        if (description != null && !description.trim().isEmpty()) {
            transaction.setDescription(description);
        } else {
            transaction.setDescription(type == TransactionType.ATM_DEPOSIT ? 
                    "ATM Deposit" : "ATM Withdrawal");
        }
        
        // Save and return
        return transactionRepository.save(transaction);
    }
    
    /**
     * Generates a unique transaction reference with format: TRX-{TYPE}-{TIMESTAMP}-{RANDOM}
     * @param type The transaction type
     * @return A unique transaction reference string
     */
    private String generateTransactionReference(TransactionType type) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = now.format(formatter);
        String randomPart = UUID.randomUUID().toString().substring(0, 8);
        
        String typePrefix;
        switch (type) {
            case ATM_DEPOSIT:
                typePrefix = "DEP";
                break;
            case ATM_WITHDRAWAL:
                typePrefix = "WDR";
                break;
            case TRANSFER:
                typePrefix = "TRF";
                break;
            default:
                typePrefix = "TRX";
        }
        
        return String.format("TRX-%s-%s-%s", typePrefix, timestamp, randomPart);
    }
}