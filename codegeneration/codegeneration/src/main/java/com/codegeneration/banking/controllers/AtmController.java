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
        return processAtmTransaction(request, TransactionType.ATM_DEPOSIT, "deposit");
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
        return processAtmTransaction(request, TransactionType.ATM_WITHDRAWAL, "withdrawal");
    }

    /**
     * Common method to process ATM transactions (deposits and withdrawals)
     */
    private ResponseEntity<AtmTransactionResponse> processAtmTransaction(
            AtmTransactionRequest request, 
            TransactionType transactionType, 
            String operationType) {
        
        log.info("Processing {} request: {}", operationType, request);
        
        try {
            // Step 1: Validate authentication and get account
            Account account = validateAndGetAccount(request.getAccountNumber(), operationType);
            
            // Step 2: Validate transaction limits
            BigDecimal amount = BigDecimal.valueOf(request.getAmount());
            validateTransactionLimits(account, amount, transactionType);
            
            // Step 3: Process the transaction
            processAccountTransaction(account, amount, transactionType);
            
            // Step 4: Create transaction record
            Transaction transaction = transactionService.createAtmTransaction(
                    account, 
                    request.getAmount(), 
                    transactionType, 
                    request.getDescription()
            );
            
            // Step 5: Build and return success response
            return buildSuccessResponse(transaction, account);
            
        } catch (InsufficientFundsException e) {
            return buildErrorResponse(400, e.getMessage(), operationType + " insufficient funds");
        } catch (IllegalArgumentException e) {
            return buildErrorResponse(400, e.getMessage(), operationType + " limit violation");
        } catch (ResourceNotFoundException e) {
            return buildErrorResponse(404, e.getMessage(), operationType + " account not found");
        } catch (Exception e) {
            log.error("Error processing " + operationType, e);
            return buildErrorResponse(500, "An error occurred while processing your " + operationType, operationType + " error");
        }
    }

    /**
     * Validates authentication and retrieves the account
     */
    private Account validateAndGetAccount(String accountNumber, String operationType) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Unauthorized {} attempt for account: {}", operationType, accountNumber);
            throw new SecurityException("User not authenticated");
        }
        
        String username = authentication.getName();
        Account account = accountService.getAccountByNumberAndUsername(accountNumber, username);
        
        if (account == null) {
            throw new ResourceNotFoundException("Account not found: " + accountNumber);
        }
        
        return account;
    }

    /**
     * Validates transaction limits based on transaction type
     */
    private void validateTransactionLimits(Account account, BigDecimal amount, TransactionType transactionType) {
        // Convert amount to EUR standard using xaffs currency exchange service
        BigDecimal amountInEur = currencyExchangeService.convertAmount(amount, account.getCurrency(), Currency.EUR);
        
        if (transactionType == TransactionType.ATM_DEPOSIT) {
            validateDepositLimits(account, amount, amountInEur);
        } else if (transactionType == TransactionType.ATM_WITHDRAWAL) {
            validateWithdrawalLimits(account, amount, amountInEur);
        }
    }

    /**
     * Validates deposit limits
     */
    private void validateDepositLimits(Account account, BigDecimal amount, BigDecimal amountInEur) {
        BigDecimal singleLimitInEur = currencyExchangeService.convertAmount(
                account.getSingleTransferLimit(), account.getCurrency(), Currency.EUR);
        BigDecimal dailyLimitInEur = currencyExchangeService.convertAmount(
                account.getDailyTransferLimit(), account.getCurrency(), Currency.EUR);
        BigDecimal transferUsedInEur = currencyExchangeService.convertAmount(
                account.getTransferUsedToday(), account.getCurrency(), Currency.EUR);
        
        validateLimits(amountInEur, singleLimitInEur, dailyLimitInEur, transferUsedInEur, 
                "transfer", "Deposit");
    }

    /**
     * Validates withdrawal limits
     */
    private void validateWithdrawalLimits(Account account, BigDecimal amount, BigDecimal amountInEur) {
        BigDecimal singleLimitInEur = currencyExchangeService.convertAmount(
                account.getSingleWithdrawalLimit(), account.getCurrency(), Currency.EUR);
        BigDecimal dailyLimitInEur = currencyExchangeService.convertAmount(
                account.getDailyWithdrawalLimit(), account.getCurrency(), Currency.EUR);
        BigDecimal withdrawalUsedInEur = currencyExchangeService.convertAmount(
                account.getWithdrawalUsedToday(), account.getCurrency(), Currency.EUR);
        
        validateLimits(amountInEur, singleLimitInEur, dailyLimitInEur, withdrawalUsedInEur, 
                "withdrawal", "Withdrawal");
    }

    /**
     * Common limit validation logic
     */
    private void validateLimits(BigDecimal amountInEur, BigDecimal singleLimitInEur, 
                               BigDecimal dailyLimitInEur, BigDecimal usedTodayInEur, 
                               String limitType, String operationType) {
        // Check single transaction limit
        if (amountInEur.compareTo(singleLimitInEur) > 0) {
            throw new IllegalArgumentException(String.format(
                    "%s amount exceeds single %s limit of %s EUR equivalent", 
                    operationType, limitType, singleLimitInEur));
        }
        
        // Check daily limit
        BigDecimal newTotalInEur = usedTodayInEur.add(amountInEur);
        if (newTotalInEur.compareTo(dailyLimitInEur) > 0) {
            throw new IllegalArgumentException(String.format(
                    "%s amount exceeds daily %s limit of %s EUR equivalent", 
                    operationType, limitType, dailyLimitInEur));
        }
    }

    /**
     * Processes the account transaction based on type
     */
    private void processAccountTransaction(Account account, BigDecimal amount, TransactionType transactionType) {
        if (transactionType == TransactionType.ATM_DEPOSIT) {
            accountService.increaseBalance(account, amount);
            account.updateTransferUsed(amount);
        } else if (transactionType == TransactionType.ATM_WITHDRAWAL) {
            accountService.decreaseBalance(account, amount);
            account.updateWithdrawalUsed(amount);
        }
        accountService.saveAccount(account);
    }

    /**
     * Builds a success response
     */
    private ResponseEntity<AtmTransactionResponse> buildSuccessResponse(Transaction transaction, Account account) {
        AtmTransactionResponse response = AtmTransactionResponse.builder()
                .success(true)
                .transactionReference(transaction.getTransactionReference())
                .updatedBalance(account.getBalance().doubleValue())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Builds an error response
     */
    private ResponseEntity<AtmTransactionResponse> buildErrorResponse(int statusCode, String errorMessage, String logMessage) {
        if (statusCode == 400) {
            log.warn(logMessage + ": {}", errorMessage);
        } else {
            log.error(logMessage + ": {}", errorMessage);
        }
        
        AtmTransactionResponse response = AtmTransactionResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
        
        return ResponseEntity.status(statusCode).body(response);
    }
}