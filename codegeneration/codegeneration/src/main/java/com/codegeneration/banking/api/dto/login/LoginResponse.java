package com.codegeneration.banking.api.dto.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT authentication token
     */
    private String token;

    /**
     * JWT refresh token for obtaining new access tokens
     */
    private String refreshToken;

    /**
     * User information
     */
    private UserDTO user;

    /**
     * Token expiration time in seconds
     */
    private Long expiresIn;
}