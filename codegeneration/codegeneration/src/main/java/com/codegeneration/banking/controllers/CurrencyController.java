package com.codegeneration.banking.controllers;

import com.codegeneration.banking.api.dto.currency.CurrencyExchangeDTO;
import com.codegeneration.banking.api.enums.Currency;
import com.codegeneration.banking.api.service.interfaces.CurrencyExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
@Tag(name = "Currency", description = "Currency API")
@Slf4j
public class CurrencyController extends BaseController {

    private final CurrencyExchangeService currencyExchangeService;

    @Operation(summary = "Get exchange rate", description = "Get exchange rate between two currencies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved exchange rate",
                    content = @Content(schema = @Schema(implementation = CurrencyExchangeDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid currency parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/exchange-rate")
    public ResponseEntity<CurrencyExchangeDTO> getExchangeRate(
            @RequestParam Currency fromCurrency,
            @RequestParam Currency toCurrency,
            @RequestParam(required = false, defaultValue = "1") BigDecimal amount) {

        try {
            log.info("Getting exchange rate from {} to {} for amount {}", fromCurrency, toCurrency, amount);

            BigDecimal rate = currencyExchangeService.getExchangeRate(fromCurrency, toCurrency);
            BigDecimal convertedAmount = currencyExchangeService.convertAmount(amount, fromCurrency, toCurrency);

            CurrencyExchangeDTO response = CurrencyExchangeDTO.builder()
                    .fromCurrency(fromCurrency)
                    .toCurrency(toCurrency)
                    .rate(rate)
                    .originalAmount(amount)
                    .convertedAmount(convertedAmount)
                    .rateInfo(String.format("1 %s = %s %s", fromCurrency, rate, toCurrency))
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting exchange rate from {} to {}: {}", fromCurrency, toCurrency, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Convert amount", description = "Convert amount from one currency to another")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully converted amount",
                    content = @Content(schema = @Schema(implementation = CurrencyExchangeDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/convert")
    public ResponseEntity<CurrencyExchangeDTO> convertAmount(
            @RequestParam Currency fromCurrency,
            @RequestParam Currency toCurrency,
            @RequestParam BigDecimal amount) {

        try {
            log.info("Converting {} {} to {}", amount, fromCurrency, toCurrency);

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().build();
            }

            BigDecimal rate = currencyExchangeService.getExchangeRate(fromCurrency, toCurrency);
            BigDecimal convertedAmount = currencyExchangeService.convertAmount(amount, fromCurrency, toCurrency);

            CurrencyExchangeDTO response = CurrencyExchangeDTO.builder()
                    .fromCurrency(fromCurrency)
                    .toCurrency(toCurrency)
                    .rate(rate)
                    .originalAmount(amount)
                    .convertedAmount(convertedAmount)
                    .rateInfo(String.format("1 %s = %s %s", fromCurrency, rate, toCurrency))
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error converting {} {} to {}: {}", amount, fromCurrency, toCurrency, e.getMessage());
            throw e;
        }
    }
}