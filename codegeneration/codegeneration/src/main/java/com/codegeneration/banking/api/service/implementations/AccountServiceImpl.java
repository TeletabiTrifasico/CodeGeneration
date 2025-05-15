package com.codegeneration.banking.api.service.implementations;

import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.User;
import com.codegeneration.banking.api.exception.ResourceNotFoundException;
import com.codegeneration.banking.api.repository.AccountRepository;
import com.codegeneration.banking.api.repository.UserRepository;
import com.codegeneration.banking.api.service.interfaces.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
    @Transactional(readOnly = true)
    public List<Account> searchAccountsByUsername(String username) {
        // Find users where the username contains the search term (case insensitive)
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(username);

        // Also search by name for better results
        users.addAll(userRepository.findByNameContainingIgnoreCase(username));

        // Remove duplicates
        users = users.stream().distinct().collect(Collectors.toList());

        // Get all accounts for these users
        List<Account> accounts = new ArrayList<>();
        for (User user : users) {
            accounts.addAll(accountRepository.findByUser(user));
        }

        return accounts;
    }

    @Override
    @Transactional(readOnly = true)
    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElse(null);
    }
}