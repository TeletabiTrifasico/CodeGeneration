package com.codegeneration.banking.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Login response data")
public class LoginResponse {

    @Schema(description = "JWT token for authentication")
    private String token;

    @Schema(description = "User information")
    private UserDTO user;

    @Schema(description = "Token expiration time in seconds")
    private Long expiresIn;
}