package com.codegeneration.banking.api.service.interfaces;

import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.Transaction;
import com.codegeneration.banking.api.entity.Transaction.TransactionType;

import java.util.List;

public interface TransactionService {

    /**
     * Get all transactions for a user across all their accounts
     *
     * @param username The username to get transactions for
     * @return List of transactions
     */
    List<Transaction> getAllTransactionsByUsername(String username);

    /**
     * Get transactions for a specific account
     *
     * @param account The account to get transactions for
     * @return List of transactions
     */
    List<Transaction> getTransactionsByAccount(Account account);
    
    /**
     * Create an ATM transaction (deposit or withdrawal)
     *
     * @param account The account involved in the transaction
     * @param amount The transaction amount (positive value)
     * @param type The transaction type (must be ATM_DEPOSIT or ATM_WITHDRAWAL)
     * @param description Optional description of the transaction
     * @return The created transaction record
     * @throws IllegalArgumentException if invalid transaction type or amount
     */
    Transaction createAtmTransaction(Account account, double amount, TransactionType type, String description);
}