package com.codegeneration.banking.api.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    private String fromAccount;
    private String toAccount;
}
