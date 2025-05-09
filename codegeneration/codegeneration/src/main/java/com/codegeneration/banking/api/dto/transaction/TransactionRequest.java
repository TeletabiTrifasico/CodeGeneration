package com.codegeneration.banking.api.dto.transaction;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for transaction-related operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    /**
     * JWT token (optional, can be provided in Authorization header)
     */
    private String token;

    /**
     * Account number for operations that require a specific account
     */
    @Size(max = 255, message = "Account number cannot exceed 255 characters")
    private String accountNumber;
}