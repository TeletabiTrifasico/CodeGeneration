package com.codegeneration.banking.api.service.interfaces;

import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.User;

import java.util.List;

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
}