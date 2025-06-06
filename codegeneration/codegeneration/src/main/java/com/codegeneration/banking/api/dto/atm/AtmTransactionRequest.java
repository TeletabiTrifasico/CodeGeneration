package com.codegeneration.banking.api.dto.atm;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for ATM transactions (deposits and withdrawals)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ATM transaction request data")
public class AtmTransactionRequest {

    /**
     * Account number for the transaction
     */
    @NotBlank(message = "Account number is required")
    @Size(max = 255, message = "Account number cannot exceed 255 characters")
    @Schema(description = "Account number for the transaction", example = "NL01INHO0000000001")
    private String accountNumber;

    /**
     * Transaction amount (must be positive)
     */
    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be greater than zero")
    @Schema(description = "Transaction amount", example = "100.00")
    private Double amount;

    /**
     * Optional transaction description
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Optional transaction description", example = "ATM deposit at XYZ location")
    private String description;
}