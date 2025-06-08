// src/test/java/com/codegeneration/banking/ATM/AtmOperationsTest.java
package com.codegeneration.banking.ATM;

import com.codegeneration.banking.api.dto.atm.AtmTransactionRequest;
import com.codegeneration.banking.base.BaseControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AtmOperationsTest extends BaseControllerTest {

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        jwtToken = authenticateAndGetToken();      // get token once
        setupAccountAndTransactionMocks();         // prepare mocks
    }

    @Test
    @DisplayName("Deposit succeeds with valid JWT")
    void depositMoney_Success() throws Exception {
        var req = AtmTransactionRequest.builder()
                    .accountNumber(TEST_ACCOUNT_NUMBER)
                    .amount(100.0)
                    .description("Deposit test")
                    .build();

        mockMvc.perform(post("/api/atm/deposit")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.transactionReference", is("TRX123456")));
    }

    @Test
    @DisplayName("Withdraw succeeds with valid JWT")
    void withdrawMoney_Success() throws Exception {
        var req = AtmTransactionRequest.builder()
                    .accountNumber(TEST_ACCOUNT_NUMBER)
                    .amount(50.0)
                    .description("Withdraw test")
                    .build();

        mockMvc.perform(post("/api/atm/withdraw")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("Operation fails without JWT")
    void atmOperation_Unauthorized() throws Exception {
        var req = AtmTransactionRequest.builder()
                    .accountNumber(TEST_ACCOUNT_NUMBER)
                    .amount(100.0)
                    .description("No JWT")
                    .build();

        mockMvc.perform(post("/api/atm/deposit")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(req)))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }
}
