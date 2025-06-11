package com.codegeneration.banking.api.service.implementations;

import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.User;
import com.codegeneration.banking.api.enums.Currency;
import com.codegeneration.banking.api.exception.InsufficientFundsException;
import com.codegeneration.banking.api.exception.ResourceNotFoundException;
import com.codegeneration.banking.api.repository.AccountRepository;
import com.codegeneration.banking.api.repository.UserRepository;
import com.codegeneration.banking.api.service.interfaces.AccountService;
import com.codegeneration.banking.api.dto.account.CreateAccountRequest;
import com.codegeneration.banking.api.dto.LimitUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    }

    @Override
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
    }    @Override
    @Transactional
    public Account createAccount(CreateAccountRequest createAccountRequest, User user) {
        System.out.println("Creating new account for user: " + user.getUsername());
        
        try {
            // Generate unique account number (IBAN format)
            String accountNumber = generateUniqueAccountNumber();
            
            // Set default currency if not provided
            Currency currency = createAccountRequest.getCurrency();
            if (currency == null) {
                currency = Currency.EUR; // Default to EUR
            }
            
            // Create new account entity using builder pattern with proper defaults
            Account account = Account.builder()
                    .accountNumber(accountNumber)
                    .accountName(createAccountRequest.getAccountName())
                    .accountType(createAccountRequest.getAccountType())
                    .currency(currency)
                    .user(user)
                    .dailyWithdrawalLimit(new BigDecimal("5000.00")) // Set to 5k as requested
                    .build();
            
            // Save the account
            Account savedAccount = accountRepository.save(account);
            
            System.out.println("Account created successfully with number: " + accountNumber + " for user: " + user.getUsername());
            
            return savedAccount;
            
        } catch (Exception e) {
            System.err.println("Error creating account for user: " + user.getUsername() + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create account: " + e.getMessage(), e);
        }
    }
    @Transactional
    public Account setAccountAsDisabled(String accountNumber) {
        try {
            Account account = accountRepository.findByAccountNumber(accountNumber).get();
            account.setDisabled(true);
            accountRepository.save(account);
            return account;

        } catch (Exception e) {
            System.err.println("Error disabling account " + accountNumber + " - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to disable account: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a unique account number in IBAN format (NL + 2 digits + bank code + 10 digits)
     */
    private String generateUniqueAccountNumber() {
        String accountNumber;
        Random random = new Random();
        
        do {
            // Generate IBAN format: NL + 2 check digits + 4 bank code + 10 account digits
            int checkDigits = 10 + random.nextInt(90); // 10-99
            String bankCode = "ABNA"; // Example bank code
            long accountDigits = 1000000000L + (long)(random.nextDouble() * 9000000000L);
            
            accountNumber = String.format("NL%02d%s%010d", checkDigits, bankCode, accountDigits);
            
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());
        
        return accountNumber;
    }
    @Override
    @jakarta.transaction.Transactional
    public Account editLimits(LimitUpdateRequest limitUpdateRequest) {
        try {
            String accountNumber = limitUpdateRequest.getAccountNumber();
            Account account = getAccountByNumber(accountNumber);
            //Could be moved to account entity possibly to look cleaner
            account.setDailyTransferLimit(BigDecimal.valueOf(limitUpdateRequest.getDailyTransferLimit()));
            account.setSingleTransferLimit(BigDecimal.valueOf(limitUpdateRequest.getSingleTransferLimit()));
            account.setDailyWithdrawalLimit(BigDecimal.valueOf(limitUpdateRequest.getDailyWithdrawalLimit()));
            account.setSingleWithdrawalLimit(BigDecimal.valueOf(limitUpdateRequest.getSingleWithdrawalLimit()));
            accountRepository.save(account);
            return account;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}