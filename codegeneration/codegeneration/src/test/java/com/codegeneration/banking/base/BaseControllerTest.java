// src/test/java/com/codegeneration/banking/base/BaseControllerTest.java
package com.codegeneration.banking.base;

import com.codegeneration.banking.api.dto.login.LoginRequest;
import com.codegeneration.banking.api.dto.login.LoginResponse;
import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.Transaction;
import com.codegeneration.banking.api.entity.Transaction.TransactionType;
import com.codegeneration.banking.api.service.implementations.TokenBlacklistService;
import com.codegeneration.banking.api.service.interfaces.AccountService;
import com.codegeneration.banking.api.service.interfaces.AuthService;
import com.codegeneration.banking.api.service.interfaces.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class BaseControllerTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected AuthService authService;

    @MockBean protected AccountService accountService;
    @MockBean protected TransactionService transactionService;
    @MockBean protected TokenBlacklistService tokenBlacklistService;    protected static final String TEST_USERNAME        = "user1";
   
   //set up test constants
    protected static final String TEST_PASSWORD        = "user123";
    protected static final String TEST_ACCOUNT_NUMBER  = "NL99BANK012345679";
    protected static final BigDecimal INITIAL_BALANCE  = BigDecimal.valueOf(1000.0);
    protected static final BigDecimal SINGLE_TRANSFER_LIMIT = BigDecimal.valueOf(3000.0);
    protected static final BigDecimal DAILY_TRANSFER_LIMIT = BigDecimal.valueOf(5000.0);
    protected static final BigDecimal SINGLE_WITHDRAWAL_LIMIT = BigDecimal.valueOf(500.0);
    protected static final BigDecimal DAILY_WITHDRAWAL_LIMIT = BigDecimal.valueOf(5000.0);

    /** Logs in and returns a valid JWT token. */
    protected String authenticateAndGetToken() throws Exception {
        var login = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper
            .readValue(result.getResponse().getContentAsString(), LoginResponse.class)
            .getToken();

        assertNotNull(token);
        return token;
    }    /** Creates test account with proper limits and balance */
    protected Account createTestAccount() {
        Account account = new Account();
        
        // Set fields using reflection to bypass Lombok issues
        try {
            setField(account, "balance", INITIAL_BALANCE);
            setField(account, "singleTransferLimit", SINGLE_TRANSFER_LIMIT);
            setField(account, "dailyTransferLimit", DAILY_TRANSFER_LIMIT);
            setField(account, "singleWithdrawalLimit", SINGLE_WITHDRAWAL_LIMIT);
            setField(account, "dailyWithdrawalLimit", DAILY_WITHDRAWAL_LIMIT);
            setField(account, "transferUsedToday", BigDecimal.ZERO);
            setField(account, "withdrawalUsedToday", BigDecimal.ZERO);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup test account", e);
        }
        
        return account;
    }
    
    /** Creates test transaction with proper reference */
    protected Transaction createTestTransaction(Account account, double amount, TransactionType type) {
        Transaction transaction = new Transaction();
        
        try {
            setField(transaction, "transactionReference", "TRX123456");
            setField(transaction, "sourceAccount", account);
            setField(transaction, "destinationAccount", account);
            setField(transaction, "amount", BigDecimal.valueOf(amount));
            setField(transaction, "type", type);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup test transaction", e);
        }
        
        return transaction;
    }
      /** Helper to set private fields via reflection */
    private void setField(Object obj, String fieldName, Object value) throws Exception {
        var field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
    
    /** Helper to get private fields via reflection */
    private Object getField(Object obj, String fieldName) throws Exception {
        var field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }/** Prepares account and transaction mocks for ATM operations */
    protected void setupAccountAndTransactionMocks() {
        Account account = createTestAccount();
        Transaction depositTx = createTestTransaction(account, 100.0, TransactionType.ATM_DEPOSIT);
        Transaction withdrawTx = createTestTransaction(account, 50.0, TransactionType.ATM_WITHDRAWAL);

        when(accountService.getAccountByNumberAndUsername(eq(TEST_ACCOUNT_NUMBER), eq(TEST_USERNAME)))
            .thenReturn(account);

        when(transactionService.createAtmTransaction(any(), eq(100.0), eq(TransactionType.ATM_DEPOSIT), anyString()))
            .thenReturn(depositTx);
        
        when(transactionService.createAtmTransaction(any(), eq(50.0), eq(TransactionType.ATM_WITHDRAWAL), anyString()))
            .thenReturn(withdrawTx);        // Mock balance updates - simulate actual balance changes
        doAnswer(invocation -> {
            Account acc = invocation.getArgument(0);
            BigDecimal amount = invocation.getArgument(1);
            try {
                BigDecimal currentBalance = (BigDecimal) getField(acc, "balance");
                setField(acc, "balance", currentBalance.add(amount));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }).when(accountService).increaseBalance(any(), any(BigDecimal.class));        doAnswer(invocation -> {
            Account acc = invocation.getArgument(0);
            BigDecimal amount = invocation.getArgument(1);
            try {
                BigDecimal currentBalance = (BigDecimal) getField(acc, "balance");
                setField(acc, "balance", currentBalance.subtract(amount));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }).when(accountService).decreaseBalance(any(), any(BigDecimal.class));

        when(accountService.saveAccount(any(Account.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0); // Return the same account that was passed in
        });
    }
}

