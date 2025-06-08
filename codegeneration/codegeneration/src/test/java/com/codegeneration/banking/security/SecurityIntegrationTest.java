// src/test/java/com/codegeneration/banking/security/SecurityIntegrationTest.java
package com.codegeneration.banking.security;

import com.codegeneration.banking.api.dto.login.LoginRequest;
import com.codegeneration.banking.api.dto.login.LoginResponse;
import com.codegeneration.banking.api.service.implementations.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean TokenBlacklistService tokenBlacklistService;

    private String userToken, empToken;

    @BeforeEach
    void init() throws Exception {
        userToken = getToken("user1","user123");
        empToken  = getToken("employee","employee123");
    }

    private String getToken(String u, String p) throws Exception {
        var req = new LoginRequest(u, p);
        var mvc = mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andReturn();

        String t = objectMapper
            .readValue(mvc.getResponse().getContentAsString(), LoginResponse.class)
            .getToken();
        assertNotNull(t);
        return t;
    }

    @Test
    void publicEndpointAccessible() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"x\",\"password\":\"x\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointsRequireAuth() throws Exception {
        mockMvc.perform(get("/api/accounts")).andExpect(status().isUnauthorized());
    }

    @Test
    void roleBasedAccess() throws Exception {
        // regular user → forbidden
        mockMvc.perform(get("/api/accounts/all")
                .header("Authorization","Bearer "+userToken))
            .andExpect(status().isForbidden());

        // employee → OK
        mockMvc.perform(get("/api/accounts/all")
                .header("Authorization","Bearer "+empToken))
            .andExpect(status().isOk());
    }
}
