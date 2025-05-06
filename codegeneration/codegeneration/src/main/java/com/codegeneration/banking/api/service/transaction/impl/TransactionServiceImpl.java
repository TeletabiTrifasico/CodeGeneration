package com.codegeneration.banking.api.service.transaction.impl;

import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.Transaction;
import com.codegeneration.banking.api.repository.AccountRepository;
import com.codegeneration.banking.api.repository.TransactionRepository;
import com.codegeneration.banking.api.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getAllTransactionsByUsername(String username) {

        List<Account> userAccounts = accountRepository.findByUserUsername(username);

        List<Transaction> allTransactions = new ArrayList<>();
        for (Account account : userAccounts) {

            allTransactions.addAll(transactionRepository.findBySourceAccountOrDestinationAccount(account, account));
        }

        return allTransactions;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsByAccount(Account account) {

        return transactionRepository.findBySourceAccountOrDestinationAccount(account, account);
    }
}