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

import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
    public void decreaseBalance(Account account, BigDecimal amount) {
        // TODO: Implement decreaseBalance logic
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void increaseBalance(Account account, BigDecimal amount) {
        // TODO: Implement increaseBalance logic
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Account getAccountByNumber(String accountNumber) {
        // TODO: Implement getAccountByNumber logic
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Account> searchAccountsByUsername(String username) {
        // TODO: Implement searchAccountsByUsername logic
        throw new UnsupportedOperationException("Not implemented yet");
    }
}