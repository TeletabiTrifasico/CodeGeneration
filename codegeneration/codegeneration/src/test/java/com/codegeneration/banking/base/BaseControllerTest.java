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
    @MockBean protected TokenBlacklistService tokenBlacklistService;

    protected static final String TEST_USERNAME        = "user1";
    protected static final String TEST_PASSWORD        = "user123";
    protected static final String TEST_ACCOUNT_NUMBER  = "NL99BANK012345679";

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
    }

    /** Prepares a test account and transaction for any ATM operation. */
    protected void setupAccountAndTransactionMocks() {
        // Initialize the test account with the minimum required fields
        Account account = new Account(1L, TEST_ACCOUNT_NUMBER, BigDecimal.valueOf(1000.0));
        
        // Explicitly set balance field through reflection since the constructor is empty
        try {
            java.lang.reflect.Field balanceField = Account.class.getDeclaredField("balance");
            balanceField.setAccessible(true);
            balanceField.set(account, BigDecimal.valueOf(1000.0));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set mock account balance", e);
        }
        
        // Initialize transaction with necessary fields
        Transaction tx = new Transaction(1L, "TRX123456", account, account, BigDecimal.valueOf(100.0), TransactionType.ATM_DEPOSIT);
        
        // Explicitly set fields through reflection if needed
        try {
            java.lang.reflect.Field refField = Transaction.class.getDeclaredField("transactionReference");
            refField.setAccessible(true);
            refField.set(tx, "TRX123456");
        } catch (Exception e) {
            // Ignore reflection errors, the mock will still work
        }

        when(accountService.getAccountByNumberAndUsername(eq(TEST_ACCOUNT_NUMBER), eq(TEST_USERNAME)))
            .thenReturn(account);

        when(transactionService.createAtmTransaction(any(), anyDouble(), any(), anyString()))
            .thenReturn(tx);

        doNothing().when(accountService).increaseBalance(any(), any(BigDecimal.class));
        doNothing().when(accountService).decreaseBalance(any(), any(BigDecimal.class));
    }
}

