package com.codegeneration.banking.api.dto.atm;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for ATM transactions (deposits and withdrawals)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ATM transaction response data")
public class AtmTransactionResponse {

    /**
     * Indicates if the transaction was successful
     */
    @Schema(description = "Whether the transaction was successful")
    private boolean success;

    /**
     * Transaction reference (if transaction was successful)
     */
    @Schema(description = "Transaction reference number (if successful)")
    private String transactionReference;

    /**
     * Updated account balance after transaction (if successful)
     */
    @Schema(description = "Updated account balance (if successful)")
    private Double updatedBalance;

    /**
     * Error message (if transaction failed)
     */
    @Schema(description = "Error message (if transaction failed)")
    private String errorMessage;
}