package com.codegeneration.banking.api.service.interfaces;

import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.User;
import com.codegeneration.banking.api.dto.account.CreateAccountRequest;

import java.math.BigDecimal;
import java.util.List;

import com.codegeneration.banking.api.dto.LimitUpdateRequest;
import com.codegeneration.banking.api.dto.account.AccountResponse;

public interface AccountService {

    /**
     * Get all accounts for a specific username
     *
     * @param username The username to get accounts for
     * @return List of accounts
     */
    List<Account> getAccountsByUsername(String username);

    /**
     * Get a specific account by its number for a specific username
     *
     * @param accountNumber The account number
     * @param username The username of the account owner
     * @return The account if found, null if not found or not owned by the user
     */
    Account getAccountByNumberAndUsername(String accountNumber, String username);

    /**
     * Get all accounts for a specific user
     *
     * @param user The user to get accounts for
     * @return List of accounts
     */
    List<Account> getAccountsByUser(User user);

    /**
     * Get a specific account by its number for a specific user
     *
     * @param accountNumber The account number
     * @param user The user who owns the account
     * @return The account if found, null if not found or not owned by the user
     */
    Account getAccountByNumberAndUser(String accountNumber, User user);
    
    /**
     * Increase account balance
     *
     * @param account The account to update
     * @param amount Amount to increase (positive value)
     * @throws IllegalArgumentException if amount is negative
     */
    void increaseBalance(Account account, BigDecimal amount);
    
    /**
     * Decrease account balance
     *
     * @param account The account to update
     * @param amount Amount to decrease (positive value)
     * @throws IllegalArgumentException if amount is negative
     * @throws InsufficientFundsException if amount exceeds available balance
     */
    void decreaseBalance(Account account, BigDecimal amount);

    /**
     * Search for accounts by username (partial match)
     *
     * @param username The username to search for (can be partial)
     * @return List of accounts matching the search criteria
     */
    List<Account> searchAccountsByUsername(String username);

    /**
     * Get an account by account number
     *
     * @param accountNumber The account number
     * @return The account if found, null otherwise
     */
    Account getAccountByNumber(String accountNumber);

    /**
     * Create a new account for a user
     *
     * @param createAccountRequest The account creation request
     * @param user The user who will own the account
     * @return The created account
     */
    Account createAccount(CreateAccountRequest createAccountRequest, User user);

    /**
     * Set account as disabled
     *
     * @param accountNumber The account to disable
     * @return The deleted account incase it's needed at some point
     */
    Account setAccountAsDisabled(String accountNumber);
    

    /**
     * Edit limits for an account
     * 
     * @param limitUpdateRequest The update request
     * @return The updated account
     */
    Account editLimits(LimitUpdateRequest limitUpdateRequest);
}