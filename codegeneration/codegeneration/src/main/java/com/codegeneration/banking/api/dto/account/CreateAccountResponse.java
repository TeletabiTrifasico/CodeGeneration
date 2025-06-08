package com.codegeneration.banking.api.dto.account;

import com.codegeneration.banking.api.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload for account creation")
public class CreateAccountResponse {

    @Schema(description = "Generated account number (IBAN)", example = "NL91ABNA0417164300")
    private String accountNumber;

    @Schema(description = "Account name", example = "My Savings Account")
    private String accountName;

    @Schema(description = "Account type", example = "SAVINGS")
    private String accountType;

    @Schema(description = "Account currency", example = "EUR")
    private Currency currency;

    @Schema(description = "Current balance", example = "0.00")
    private BigDecimal balance;

    @Schema(description = "Daily transfer limit", example = "5000.00")
    private BigDecimal dailyTransferLimit;

    @Schema(description = "Account creation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Owner username", example = "john.doe")
    private String ownerUsername;
}
