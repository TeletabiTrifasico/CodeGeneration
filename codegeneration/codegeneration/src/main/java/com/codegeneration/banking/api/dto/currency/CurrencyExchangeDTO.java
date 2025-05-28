package com.codegeneration.banking.api.dto.currency;

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
public class CurrencyExchangeDTO {
    private Currency fromCurrency;
    private Currency toCurrency;
    private BigDecimal rate;
    private BigDecimal originalAmount;
    private BigDecimal convertedAmount;
    private String rateInfo;
}