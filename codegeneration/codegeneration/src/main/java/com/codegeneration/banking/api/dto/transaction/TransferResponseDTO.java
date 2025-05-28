package com.codegeneration.banking.api.dto.transaction;

import com.codegeneration.banking.api.dto.currency.CurrencyExchangeDTO;
import com.codegeneration.banking.api.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponseDTO {
    private TransactionDTO transaction;
    private boolean currencyExchangeApplied;
    private CurrencyExchangeDTO exchangeInfo;
    private String message;
}