// src/test/java/com/codegeneration/banking/auth/AuthControllerTest.java
package com.codegeneration.banking.auth;

import com.codegeneration.banking.api.dto.login.LoginRequest;
import com.codegeneration.banking.api.dto.login.LoginResponse;
import com.codegeneration.banking.api.service.implementations.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean TokenBlacklistService tokenBlacklistService;

    private static final String USER = "user1", PWD = "user123";

    @Test
    void login_Success() throws Exception {
        LoginRequest req = new LoginRequest(USER, PWD);
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.user.username").value(USER))
            .andDo(mvc -> {
                var resp = objectMapper
                    .readValue(mvc.getResponse().getContentAsString(), LoginResponse.class);
                assertNotNull(resp.getToken());
            });
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        var req = new LoginRequest(USER, "wrongPassword011235");
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }
}
