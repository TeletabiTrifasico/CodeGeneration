package com.codegeneration.banking.api.service.interfaces;

import com.codegeneration.banking.api.entity.Transaction;
import com.codegeneration.banking.api.dto.transactionfilter.TransactionFilterRequest;

import java.util.List;

public interface TransactionFilterService {

    /**
     * Filter transactions based on provided criteria
     *
     * @param transactions Base list of transactions to filter
     * @param filterRequest Filter criteria
     * @return Filtered list of transactions
     */
    List<Transaction> filterTransactions(List<Transaction> transactions, TransactionFilterRequest filterRequest);

    /**
     * Get filtered transactions for a username
     *
     * @param username The username to get transactions for
     * @param filterRequest Filter criteria
     * @return Filtered list of transactions
     */
    List<Transaction> getFilteredTransactionsByUsername(String username, TransactionFilterRequest filterRequest);

    /**
     * Get filtered transactions for a specific account
     *
     * @param accountNumber The account number
     * @param username The username of the account owner
     * @param filterRequest Filter criteria
     * @return Filtered list of transactions
     */
    List<Transaction> getFilteredTransactionsByAccount(String accountNumber, String username, TransactionFilterRequest filterRequest);
}