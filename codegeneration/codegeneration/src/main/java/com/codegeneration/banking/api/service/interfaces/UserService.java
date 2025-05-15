package com.codegeneration.banking.api.service.interfaces;

import java.util.List;

import com.codegeneration.banking.api.entity.User;


public interface UserService {

    /**
     * Get all transactions for a user across all their accounts
     *
     * @return List of users
     */
    List<User> getAllUsers();

    /**
     * Get transactions for a specific account
     *
     * @param pageNumber the page to get users from
     * @param limit the amount of users per page
     * @return List of transactions
     */
    List<User> getUsersByPage(Number pageNumber, Number limit);
}