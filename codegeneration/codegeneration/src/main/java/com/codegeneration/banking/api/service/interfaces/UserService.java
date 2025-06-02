package com.codegeneration.banking.api.service.interfaces;

import java.util.List;
import java.util.Optional;

import com.codegeneration.banking.api.entity.User;


public interface UserService {

    /**
     * Get all users
     *
     * @return List of users
     */
    List<User> getAllUsers();

    /**
     * Get users through pagination
     *
     * @param pageNumber the page to get users from
     * @param limit the amount of users per page
     * @return List of transactions
     */
    List<User> getUsersByPage(Number pageNumber, Number limit);

    /**
     * Get user based on id
     *
     * @param id the user's id
     * @return List with user
     */
    List<User> getUserById(Number id);

    /**
     * Get user based on username
     *
     * @param username the user's username
     * @return List with user
     */
    Optional<User> getUserByUsername(String username);
}