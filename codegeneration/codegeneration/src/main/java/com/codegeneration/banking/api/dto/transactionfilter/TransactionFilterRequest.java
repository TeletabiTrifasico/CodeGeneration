package com.codegeneration.banking.api.dto.transactionfilter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionFilterRequest {

    // Date range filters
    private LocalDate startDate;
    private LocalDate endDate;

    // Amount filters
    private BigDecimal amountLessThan;
    private BigDecimal amountGreaterThan;
    private BigDecimal amountEqualTo;

    // IBAN filter
    private String iban;

    // Transaction type filter
    private String transactionType;

    // Transaction status filter
    private String transactionStatus;

    // Description search
    private String description;
}