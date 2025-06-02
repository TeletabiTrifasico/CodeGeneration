
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
public class LimitUpdateRequest {

    @NotBlank(message = "value is required")
    private int dailyTransferLimit;
    @NotBlank(message = "value is required")
    private int singleTransferLimit;
    @NotBlank(message = "value is required")
    private int dailyWithdrawalLimit;
    @NotBlank(message = "value is required")
    private int singleWithdrawalLimit;

    // Getters and Setters
}