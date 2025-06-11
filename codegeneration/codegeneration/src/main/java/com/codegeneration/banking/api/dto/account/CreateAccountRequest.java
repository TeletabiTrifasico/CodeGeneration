package com.codegeneration.banking.api.dto.account;

import com.codegeneration.banking.api.entity.User;
import com.codegeneration.banking.api.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for creating a new account")
public class CreateAccountRequest {

    @NotBlank(message = "Account name is required")
    @Size(min = 2, max = 50, message = "Account name must be between 2 and 50 characters")
    @Schema(description = "Name for the new account", example = "My Savings Account")
    private String accountName;

    @NotBlank(message = "Account type is required")
    @Size(min = 2, max = 20, message = "Account type must be between 2 and 20 characters")
    @Schema(description = "Type of account", example = "SAVINGS")
    private String accountType;

    @NotNull(message = "Currency is required")
    @Schema(description = "Currency for the account", example = "EUR")
    private Currency currency;

    @Schema(description = "Optional user ID to assign the account to")
    private Integer userId;  // optional, can be null
}
