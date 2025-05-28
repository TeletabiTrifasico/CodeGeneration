package com.codegeneration.banking.api.service.interfaces;

import com.codegeneration.banking.api.enums.Currency;

import java.math.BigDecimal;

public interface CurrencyExchangeService {

    /**
     * Get exchange rate from source currency to target currency
     *
     * @param fromCurrency Source currency
     * @param toCurrency Target currency
     * @return Exchange rate
     */
    BigDecimal getExchangeRate(Currency fromCurrency, Currency toCurrency);

    /**
     * Convert amount from one currency to another
     *
     * @param amount Amount to convert
     * @param fromCurrency Source currency
     * @param toCurrency Target currency
     * @return Converted amount
     */
    BigDecimal convertAmount(BigDecimal amount, Currency fromCurrency, Currency toCurrency);

    /**
     * Check if currency conversion is needed
     *
     * @param fromCurrency Source currency
     * @param toCurrency Target currency
     * @return true if conversion is needed, false otherwise
     */
    boolean isConversionNeeded(Currency fromCurrency, Currency toCurrency);
}