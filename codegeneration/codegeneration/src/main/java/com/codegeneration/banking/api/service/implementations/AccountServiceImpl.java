package com.codegeneration.banking.api.service.implementations;

import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.User;
import com.codegeneration.banking.api.exception.InsufficientFundsException;
import com.codegeneration.banking.api.exception.ResourceNotFoundException;
import com.codegeneration.banking.api.repository.AccountRepository;
import com.codegeneration.banking.api.repository.UserRepository;
import com.codegeneration.banking.api.service.interfaces.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAccountsByUsername(String username) {
        return accountRepository.findByUserUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccountByNumberAndUsername(String accountNumber, String username) {
        return accountRepository.findByAccountNumberAndUserUsername(accountNumber, username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with number: " + accountNumber + " for user: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAccountsByUser(User user) {
        return accountRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccountByNumberAndUser(String accountNumber, User user) {
        return accountRepository.findByAccountNumberAndUser(accountNumber, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with number: " + accountNumber + " for user ID: " + user.getId()));
    }    @Override
    @Transactional
    public void decreaseBalance(Account account, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        // Check if withdrawal is allowed (limits)
        if (!account.isWithdrawalAllowed(amount)) {
            throw new InsufficientFundsException("Withdrawal exceeds daily or single transaction limits");
        }
        
        // Check if sufficient balance
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient balance. Available: " + 
                account.getBalance() + ", Requested: " + amount);
        }
        
        // Update balance and withdrawal usage
        account.setBalance(account.getBalance().subtract(amount));
        account.updateWithdrawalUsed(amount);
        
        // Save the updated account
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void increaseBalance(Account account, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        // Update balance
        account.setBalance(account.getBalance().add(amount));
        
        // Save the updated account
        accountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with number: " + accountNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> searchAccountsByUsername(String username) {
        return accountRepository.findByUserUsername(username);
    }
}