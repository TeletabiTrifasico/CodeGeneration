package com.codegeneration.banking.api.repository;

import com.codegeneration.banking.api.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find a user by username
     *
     * @param accountNumber AccountNumber
     * @return Optional of account
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Check if a account exists with specific account number
     *
     * @param accountNumber AccountNumber
     * @return True if account number exists, false otherwise
     */
    boolean existsByAccountNumber(String accountNumber);
}
