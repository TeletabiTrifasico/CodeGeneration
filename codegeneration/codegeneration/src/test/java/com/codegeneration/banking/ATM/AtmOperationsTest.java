package com.codegeneration.banking.ATM;

import com.codegeneration.banking.api.dto.atm.AtmTransactionRequest;
import com.codegeneration.banking.base.BaseControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AtmOperationsTest extends BaseControllerTest {

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        jwtToken = authenticateAndGetToken();
        setupAccountAndTransactionMocks();
    }

    @Test
    void deposit_Success() throws Exception {
        var request = AtmTransactionRequest.builder()
                .accountNumber(TEST_ACCOUNT_NUMBER)
                .amount(100.0)
                .description("Test deposit")
                .build();

        mockMvc.perform(post("/api/atm/deposit")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.transactionReference", is("TRX123456")))
            .andExpect(jsonPath("$.updatedBalance", is(1100.0))); // 1000 + 100
    }

    @Test
    void withdraw_Success() throws Exception {
        var request = AtmTransactionRequest.builder()
                .accountNumber(TEST_ACCOUNT_NUMBER)
                .amount(50.0)
                .description("Test withdrawal")
                .build();

        mockMvc.perform(post("/api/atm/withdraw")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.transactionReference", is("TRX123456")))
            .andExpect(jsonPath("$.updatedBalance", is(950.0))); // 1000 - 50
    }

    @Test
    void deposit_Unauthorized() throws Exception {
        var request = AtmTransactionRequest.builder()
                .accountNumber(TEST_ACCOUNT_NUMBER)
                .amount(100.0)
                .build();

        mockMvc.perform(post("/api/atm/deposit")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void withdraw_Unauthorized() throws Exception {
        var request = AtmTransactionRequest.builder()
                .accountNumber(TEST_ACCOUNT_NUMBER)
                .amount(50.0)
                .build();

        mockMvc.perform(post("/api/atm/withdraw")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))            .andExpect(status().isUnauthorized());
    }
}
