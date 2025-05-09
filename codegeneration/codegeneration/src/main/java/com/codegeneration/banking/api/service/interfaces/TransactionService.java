package com.codegeneration.banking.api.service.interfaces;

import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.Transaction;

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
}