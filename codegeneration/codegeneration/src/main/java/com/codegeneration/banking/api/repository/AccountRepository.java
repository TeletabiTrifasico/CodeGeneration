package com.codegeneration.banking.api.repository;

import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find all accounts for a specific user
     *
     * @param user The user to find accounts for
     * @return List of accounts
     */
    List<Account> findByUser(User user);

    /**
     * Find all accounts for a specific username
     *
     * @param username The username to find accounts for
     * @return List of accounts
     */
    List<Account> findByUserUsername(String username);

    /**
     * Find an account by account number
     *
     * @param accountNumber The account number
     * @return Optional containing the account if found
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Find an account by account number and user
     *
     * @param accountNumber The account number
     * @param user The user who owns the account
     * @return Optional containing the account if found
     */
    Optional<Account> findByAccountNumberAndUser(String accountNumber, User user);

    /**
     * Find an account by account number and username
     *
     * @param accountNumber The account number
     * @param username The username of the account owner
     * @return Optional containing the account if found
     */
    Optional<Account> findByAccountNumberAndUserUsername(String accountNumber, String username);
}