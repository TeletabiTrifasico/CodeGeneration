package com.codegeneration.banking.api.repository;

import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    /**
     * Find transactions where the account is either the source or destination
     *
     * @param sourceAccount The source account
     * @param destinationAccount The destination account
     * @return List of transactions
     */
    List<Transaction> findBySourceAccountOrDestinationAccount(Account sourceAccount, Account destinationAccount);

    /**
     * Find all transactions where the specified account is either the source or destination
     */
    List<Transaction> findAllBySourceAccountOrDestinationAccount(Account sourceAccount, Account destinationAccount);
    
    /**
     * Find all transactions where the specified username is either the source or destination account owner
     */
    List<Transaction> findAllBySourceAccountUserUsernameOrDestinationAccountUserUsername(
            String sourceUsername, String destinationUsername);
    
    /**
     * Find all transactions for a specific account number
     */
    List<Transaction> findAllBySourceAccountAccountNumberOrDestinationAccountAccountNumber(
            String sourceAccountNumber, String destinationAccountNumber);
}