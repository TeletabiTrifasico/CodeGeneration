package com.codegeneration.banking.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsernameRequest {

    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "Username is required")
    private String username;
}