
package com.codegeneration.banking.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimitUpdateRequest {
    @NotBlank(message = "Account number is required")
    private String accountNumber;
    @NotNull(message = "value is required")
    private int dailyTransferLimit;
    @NotNull(message = "value is required")
    private int singleTransferLimit;
    @NotNull(message = "value is required")
    private int dailyWithdrawalLimit;
    @NotNull(message = "value is required")
    private int singleWithdrawalLimit;

    // Getters and Setters
}