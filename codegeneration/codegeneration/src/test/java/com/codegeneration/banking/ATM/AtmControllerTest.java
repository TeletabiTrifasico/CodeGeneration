package com.codegeneration.banking.ATM;

import com.codegeneration.banking.api.dto.atm.AtmTransactionRequest;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the ATM Controller
 * 
 * This test uses a real JWT token to test the authentication flow along with the ATM operations.
 * It demonstrates how the frontend would interact with the backend using JWT authentication.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AtmControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private AuthService authService;
    
    // Optional: mock these services if you don't want to use the actual database
    @MockBean
    private AccountService accountService;
    
    @MockBean
    private TransactionService transactionService;
    
    @MockBean
    private TokenBlacklistService tokenBlacklistService;
      private String jwtToken;
    private static final String TEST_USERNAME = "user1"; // Use a username from your DataLoader
    private static final String TEST_PASSWORD = "user123"; // Use the password from your DataLoader
    private static final String TEST_ACCOUNT_NUMBER = "NL99BANK012345679"; // Account number for user1 from DataLoader
    
    /**
     * Before each test, get a valid JWT token by logging in
     */    @BeforeEach
    void setUp() throws Exception {
        // Create login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);
        
        // Login and get JWT token
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        // Extract token from response
        String contentAsString = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(contentAsString, LoginResponse.class);
        jwtToken = loginResponse.getToken();
        
        assertNotNull(jwtToken, "JWT token should not be null");
        
        // Setup mock responses for AccountService and TransactionService
        // Create a test account
        Account testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setAccountNumber(TEST_ACCOUNT_NUMBER);
        testAccount.setBalance(new BigDecimal("1000.00"));
        
        // Create a test transaction
        Transaction testTransaction = new Transaction();
        testTransaction.setId(1L);
        testTransaction.setTransactionReference("TRX123456");
        testTransaction.setSourceAccount(testAccount);
        testTransaction.setDestinationAccount(testAccount);
        testTransaction.setAmount(new BigDecimal("100.0"));
        
        // Configure mock responses
        when(accountService.getAccountByNumberAndUsername(eq(TEST_ACCOUNT_NUMBER), eq(TEST_USERNAME)))
            .thenReturn(testAccount);
            
        when(transactionService.createAtmTransaction(
            any(Account.class), 
            anyDouble(), 
            any(Transaction.TransactionType.class), 
            anyString()))
            .thenReturn(testTransaction);
            
        // Don't throw exceptions for balance operations
        doNothing().when(accountService).increaseBalance(any(Account.class), any(BigDecimal.class));
        doNothing().when(accountService).decreaseBalance(any(Account.class), any(BigDecimal.class));
    }
    
    /**
     * Test depositing money with JWT authentication
     */
    @Test
    @DisplayName("Should deposit money with valid JWT token")
    void depositMoney_WithJwt() throws Exception {
        // Create deposit request
        AtmTransactionRequest request = AtmTransactionRequest.builder()
                .accountNumber(TEST_ACCOUNT_NUMBER)
                .amount(100.0)
                .description("Test deposit with JWT")
                .build();
        
        // Perform deposit with JWT token
        mockMvc.perform(post("/api/atm/deposit")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }
    
    /**
     * Test withdrawing money with JWT authentication
     */
    @Test
    @DisplayName("Should withdraw money with valid JWT token")
    void withdrawMoney_WithJwt() throws Exception {
        // Create withdrawal request
        AtmTransactionRequest request = AtmTransactionRequest.builder()
                .accountNumber(TEST_ACCOUNT_NUMBER)
                .amount(50.0)
                .description("Test withdrawal with JWT")
                .build();
        
        // Perform withdrawal with JWT token
        mockMvc.perform(post("/api/atm/withdraw")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }
    
    /**
     * Test attempting operations without JWT token
     */
    @Test
    @DisplayName("Should reject operation without JWT token")
    void atmOperation_WithoutJwt() throws Exception {
        // Create deposit request
        AtmTransactionRequest request = AtmTransactionRequest.builder()
                .accountNumber(TEST_ACCOUNT_NUMBER)
                .amount(100.0)
                .description("Test deposit without JWT")
                .build();
        
        // Attempt deposit without JWT token
        mockMvc.perform(post("/api/atm/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
