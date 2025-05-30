package com.codegeneration.banking.api.service.implementations;

import com.codegeneration.banking.api.enums.Currency;
import com.codegeneration.banking.api.service.interfaces.CurrencyExchangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {

    // Hardcoded exchange rates (EUR as base currency)
    private static final Map<String, BigDecimal> EXCHANGE_RATES = new HashMap<>();

    static {
        // EUR to other currencies
        EXCHANGE_RATES.put("EUR_USD", new BigDecimal("1.08"));
        EXCHANGE_RATES.put("EUR_GBP", new BigDecimal("0.86"));
        EXCHANGE_RATES.put("EUR_CHF", new BigDecimal("0.95"));
        EXCHANGE_RATES.put("EUR_PLN", new BigDecimal("4.23"));

        // USD to other currencies
        EXCHANGE_RATES.put("USD_EUR", new BigDecimal("0.93"));
        EXCHANGE_RATES.put("USD_GBP", new BigDecimal("0.80"));
        EXCHANGE_RATES.put("USD_CHF", new BigDecimal("0.88"));
        EXCHANGE_RATES.put("USD_PLN", new BigDecimal("3.92"));

        // GBP to other currencies
        EXCHANGE_RATES.put("GBP_EUR", new BigDecimal("1.16"));
        EXCHANGE_RATES.put("GBP_USD", new BigDecimal("1.25"));
        EXCHANGE_RATES.put("GBP_CHF", new BigDecimal("1.10"));
        EXCHANGE_RATES.put("GBP_PLN", new BigDecimal("4.91"));

        // CHF to other currencies
        EXCHANGE_RATES.put("CHF_EUR", new BigDecimal("1.05"));
        EXCHANGE_RATES.put("CHF_USD", new BigDecimal("1.14"));
        EXCHANGE_RATES.put("CHF_GBP", new BigDecimal("0.91"));
        EXCHANGE_RATES.put("CHF_PLN", new BigDecimal("4.45"));

        // PLN to other currencies
        EXCHANGE_RATES.put("PLN_EUR", new BigDecimal("0.24"));
        EXCHANGE_RATES.put("PLN_USD", new BigDecimal("0.26"));
        EXCHANGE_RATES.put("PLN_GBP", new BigDecimal("0.20"));
        EXCHANGE_RATES.put("PLN_CHF", new BigDecimal("0.22"));
    }

    @Override
    public BigDecimal getExchangeRate(Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency == null || toCurrency == null) {
            throw new IllegalArgumentException("Currencies cannot be null");
        }

        if (fromCurrency == toCurrency) {
            return BigDecimal.ONE;
        }

        String rateKey = fromCurrency.name() + "_" + toCurrency.name();
        BigDecimal rate = EXCHANGE_RATES.get(rateKey);

        if (rate == null) {
            log.error("Exchange rate not found for {} to {}", fromCurrency, toCurrency);
            throw new RuntimeException("Exchange rate not available for " + fromCurrency + " to " + toCurrency);
        }

        return rate;
    }

    @Override
    public BigDecimal convertAmount(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (fromCurrency == toCurrency) {
            return amount;
        }

        BigDecimal rate = getExchangeRate(fromCurrency, toCurrency);
        BigDecimal convertedAmount = amount.multiply(rate);

        // Round to 4 decimal places
        return convertedAmount.setScale(4, RoundingMode.HALF_UP);
    }

    @Override
    public boolean isConversionNeeded(Currency fromCurrency, Currency toCurrency) {
        return fromCurrency != null && toCurrency != null && !fromCurrency.equals(toCurrency);
    }
}