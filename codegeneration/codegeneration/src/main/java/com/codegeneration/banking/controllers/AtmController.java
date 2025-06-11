package com.codegeneration.banking.controllers;

import com.codegeneration.banking.api.dto.atm.AtmTransactionRequest;
import com.codegeneration.banking.api.dto.atm.AtmTransactionResponse;
import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.Transaction;
import com.codegeneration.banking.api.entity.Transaction.TransactionType;
import com.codegeneration.banking.api.enums.Currency;
import com.codegeneration.banking.api.exception.InsufficientFundsException;
import com.codegeneration.banking.api.exception.ResourceNotFoundException;
import com.codegeneration.banking.api.service.interfaces.AccountService;
import com.codegeneration.banking.api.service.interfaces.TransactionService;
import com.codegeneration.banking.api.service.interfaces.CurrencyExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/atm")
@RequiredArgsConstructor
@Tag(name = "ATM", description = "ATM API for deposits and withdrawals")
@Slf4j
public class AtmController extends BaseController {

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final CurrencyExchangeService currencyExchangeService;

    @Operation(summary = "Deposit money", description = "Deposit money into an account via ATM")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deposit successful",
                    content = @Content(schema = @Schema(implementation = AtmTransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/deposit")
    public ResponseEntity<AtmTransactionResponse> deposit(@Valid @RequestBody AtmTransactionRequest request) {
        log.info("Processing deposit request: {}", request);
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Unauthorized deposit attempt for account: {}", request.getAccountNumber());
                return ResponseEntity.status(401).build();
            }
            
            String username = authentication.getName();
            Account account = accountService.getAccountByNumberAndUsername(request.getAccountNumber(), username);
            
            if (account == null) {
                throw new ResourceNotFoundException("Account not found: " + request.getAccountNumber());
            }
            
            // deposit
            double amount = request.getAmount();
            BigDecimal depositAmount = BigDecimal.valueOf(amount);            // Check if deposit respects daily transfer limits (deposits use transfer limits)
            // Convert both amount and limits to EUR for standardized comparison
            BigDecimal amountInEur = currencyExchangeService.convertAmount(depositAmount, account.getCurrency(), Currency.EUR);
            BigDecimal singleLimitInEur = currencyExchangeService.convertAmount(account.getSingleTransferLimit(), account.getCurrency(), Currency.EUR);
            BigDecimal dailyLimitInEur = currencyExchangeService.convertAmount(account.getDailyTransferLimit(), account.getCurrency(), Currency.EUR);
            BigDecimal transferUsedInEur = currencyExchangeService.convertAmount(account.getTransferUsedToday(), account.getCurrency(), Currency.EUR);
            
            // Check single transaction limit in EUR
            if (amountInEur.compareTo(singleLimitInEur) > 0) {
                throw new IllegalArgumentException("Deposit amount exceeds single transfer limit of " + 
                    singleLimitInEur + " EUR equivalent");
            }
            
            // Check daily limit in EUR
            BigDecimal newTotalInEur = transferUsedInEur.add(amountInEur);
            if (newTotalInEur.compareTo(dailyLimitInEur) > 0) {
                throw new IllegalArgumentException("Deposit amount exceeds daily transfer limit of " + 
                    dailyLimitInEur + " EUR equivalent");
            }
            
            // Process deposit
            accountService.increaseBalance(account, depositAmount);
            account.updateTransferUsed(depositAmount); // Update daily transfer usage for deposits
            accountService.saveAccount(account); // Save the updated account with usage tracking
            
            // Create transaction record
            Transaction transaction = transactionService.createAtmTransaction(
                    account, 
                    amount, 
                    TransactionType.ATM_DEPOSIT, 
                    request.getDescription()
            );
            
            // Build response
            AtmTransactionResponse response = AtmTransactionResponse.builder()
                    .success(true)
                    .transactionReference(transaction.getTransactionReference())
                    .updatedBalance(account.getBalance().doubleValue())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Deposit limit violation: {}", e.getMessage());
            return ResponseEntity.status(400)
                    .body(AtmTransactionResponse.builder()
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
        } catch (ResourceNotFoundException e) {
            log.error("Error processing deposit: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(AtmTransactionResponse.builder()
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error processing deposit", e);
            return ResponseEntity.status(500)
                    .body(AtmTransactionResponse.builder()
                            .success(false)
                            .errorMessage("An error occurred while processing your deposit")
                            .build());
        }
    }
    
    @Operation(summary = "Withdraw money", description = "Withdraw money from an account via ATM")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Withdrawal successful",
                    content = @Content(schema = @Schema(implementation = AtmTransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or insufficient funds"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/withdraw")
    public ResponseEntity<AtmTransactionResponse> withdraw(@Valid @RequestBody AtmTransactionRequest request) {
        log.info("Processing withdrawal request: {}", request);
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("Unauthorized withdrawal attempt for account: {}", request.getAccountNumber());
                return ResponseEntity.status(401).build();
            }
            
            String username = authentication.getName();
            Account account = accountService.getAccountByNumberAndUsername(request.getAccountNumber(), username);
            
            if (account == null) {
                throw new ResourceNotFoundException("Account not found: " + request.getAccountNumber());
            }
            
            // Validate withdrawal
            double amount = request.getAmount();
            BigDecimal withdrawalAmount = BigDecimal.valueOf(amount);            // Check if withdrawal respects withdrawal limits
            // Convert both amount and limits to EUR for standardized comparison
            BigDecimal amountInEur = currencyExchangeService.convertAmount(withdrawalAmount, account.getCurrency(), Currency.EUR);
            BigDecimal singleLimitInEur = currencyExchangeService.convertAmount(account.getSingleWithdrawalLimit(), account.getCurrency(), Currency.EUR);
            BigDecimal dailyLimitInEur = currencyExchangeService.convertAmount(account.getDailyWithdrawalLimit(), account.getCurrency(), Currency.EUR);
            BigDecimal withdrawalUsedInEur = currencyExchangeService.convertAmount(account.getWithdrawalUsedToday(), account.getCurrency(), Currency.EUR);
            
            // Check single transaction limit in EUR
            if (amountInEur.compareTo(singleLimitInEur) > 0) {
                throw new IllegalArgumentException("Withdrawal amount exceeds single withdrawal limit of " + 
                    singleLimitInEur + " EUR equivalent");
            }
            
            // Check daily limit in EUR
            BigDecimal newTotalInEur = withdrawalUsedInEur.add(amountInEur);
            if (newTotalInEur.compareTo(dailyLimitInEur) > 0) {
                throw new IllegalArgumentException("Withdrawal amount exceeds daily withdrawal limit of " + 
                    dailyLimitInEur + " EUR equivalent");
            }
            
            // Process withdrawal
            accountService.decreaseBalance(account, withdrawalAmount);
            account.updateWithdrawalUsed(withdrawalAmount); // Update daily withdrawal usage
            accountService.saveAccount(account); // Save the updated account with usage tracking
            
            // Create transaction record
            Transaction transaction = transactionService.createAtmTransaction(
                    account, 
                    amount, 
                    TransactionType.ATM_WITHDRAWAL, 
                    request.getDescription()
            );
            
            // Build response
            AtmTransactionResponse response = AtmTransactionResponse.builder()
                    .success(true)
                    .transactionReference(transaction.getTransactionReference())
                    .updatedBalance(account.getBalance().doubleValue())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (InsufficientFundsException e) {
            log.warn("Insufficient funds for withdrawal: {}", e.getMessage());
            return ResponseEntity.status(400)
                    .body(AtmTransactionResponse.builder()
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
        } catch (IllegalArgumentException e) {
            log.warn("Withdrawal limit violation: {}", e.getMessage());
            return ResponseEntity.status(400)
                    .body(AtmTransactionResponse.builder()
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
        } catch (ResourceNotFoundException e) {
            log.error("Error processing withdrawal: {}", e.getMessage());
            return ResponseEntity.status(404)
                    .body(AtmTransactionResponse.builder()
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Error processing withdrawal", e);
            return ResponseEntity.status(500)
                    .body(AtmTransactionResponse.builder()
                            .success(false)
                            .errorMessage("An error occurred while processing your withdrawal")
                            .build());
        }
    }
}