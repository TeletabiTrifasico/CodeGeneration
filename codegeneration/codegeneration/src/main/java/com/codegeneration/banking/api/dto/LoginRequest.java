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
@Schema(description = "Login request data")
public class LoginRequest {

    @Schema(description = "Username", example = "johndoe")
    private String username;

    @Schema(description = "Password", example = "password123")
    private String password;
}