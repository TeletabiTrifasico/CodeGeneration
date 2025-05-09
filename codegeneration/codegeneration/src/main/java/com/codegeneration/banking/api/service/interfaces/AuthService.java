package com.codegeneration.banking.api.service.interfaces;

import com.codegeneration.banking.api.dto.login.LoginRequest;
import com.codegeneration.banking.api.dto.login.LoginResponse;
import com.codegeneration.banking.api.dto.register.RegisterRequest;
import com.codegeneration.banking.api.dto.login.UserDTO;

/**
 * Service for authentication operations
 */
public interface AuthService {

    /**
     * Login a user
     *
     * @param loginRequest Login request containing username and password
     * @return LoginResponse with token and user info
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * Validate a JWT token
     *
     * @param token The JWT token to validate
     * @return UserDTO with user information
     */
    UserDTO validateToken(String token);

    /**
     * Register a new user
     *
     * @param registerRequest Registration request
     * @return UserDTO of the created user
     */
    UserDTO register(RegisterRequest registerRequest);

    /**
     * Get user info by username
     *
     * @param username The username
     * @return UserDTO with user information
     */
    UserDTO getUserInfo(String username);
}