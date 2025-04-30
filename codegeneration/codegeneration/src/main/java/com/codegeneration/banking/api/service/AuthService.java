package com.codegeneration.banking.api.service;

import com.codegeneration.banking.api.dto.LoginRequest;
import com.codegeneration.banking.api.dto.LoginResponse;
import com.codegeneration.banking.api.dto.UserDTO;
import com.codegeneration.banking.api.dto.RegisterRequest;

public interface AuthService {

    /**
     * Authenticate a user and generate JWT token
     *
     * @param loginRequest The login credentials
     * @return Login response with JWT token and user information
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * Validate a JWT token and return user information
     *
     * @param token The JWT token to validate
     * @return User information if token is valid
     */
    UserDTO validateToken(String token);

    /**
     * Register a new user
     *
     * @param registerRequest The registration details
     * @return DTO of the newly created user
     * @throws ConflictException if username or email already exists
     */
    UserDTO register(RegisterRequest registerRequest);
}