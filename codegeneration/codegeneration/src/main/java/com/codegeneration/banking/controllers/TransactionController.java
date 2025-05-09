package com.codegeneration.banking.controllers;

import com.codegeneration.banking.api.dto.transaction.TransactionDTO;
import com.codegeneration.banking.api.dto.transaction.TransactionResponse;
import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.Transaction;
import com.codegeneration.banking.api.exception.ResourceNotFoundException;
import com.codegeneration.banking.api.security.JwtAuthenticationFilter;
import com.codegeneration.banking.api.security.JwtTokenProvider;
import com.codegeneration.banking.api.service.interfaces.AccountService;
import com.codegeneration.banking.api.service.interfaces.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "Transaction API")
@Slf4j
public class TransactionController extends BaseController {

    private final TransactionService transactionService;
    private final AccountService accountService;

    @Operation(summary = "Get all transactions", description = "Returns all transactions belonging to the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getall")
    public ResponseEntity<TransactionResponse> getAllTransactions() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authentication found for GET /transaction/getall");
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            log.info("Processing GET /transaction/getall for user: {}", username);

            List<Transaction> transactions = transactionService.getAllTransactionsByUsername(username);

            List<TransactionDTO> transactionDTOs = transactions.stream()
                    .map(TransactionDTO::fromEntity)
                    .collect(Collectors.toList());

            TransactionResponse response = TransactionResponse.builder()
                    .transactions(transactionDTOs)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in GET /transaction/getall", e);
            throw e;
        }
    }

    @Operation(summary = "Get transactions by account number", description = "Returns all transactions for a specific account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/byaccount/{accountNumber}")
    public ResponseEntity<TransactionResponse> getTransactionsByAccount(@PathVariable String accountNumber) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authentication found for GET /transaction/byaccount/{}", accountNumber);
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            log.info("Processing GET /transaction/byaccount/{} for user: {}", accountNumber, username);

            Account account = accountService.getAccountByNumberAndUsername(accountNumber, username);
            if (account == null) {
                throw new ResourceNotFoundException("Account not found with number: " + accountNumber);
            }

            List<Transaction> transactions = transactionService.getTransactionsByAccount(account);

            List<TransactionDTO> transactionDTOs = transactions.stream()
                    .map(TransactionDTO::fromEntity)
                    .collect(Collectors.toList());

            TransactionResponse response = TransactionResponse.builder()
                    .transactions(transactionDTOs)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in GET /transaction/byaccount/{}", e);
            throw e;
        }
    }


    @PostMapping("/transfer")
    public ResponseEntity postTransaction(@RequestBody TransactionDTO transactionDTO) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if(authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authentication found for GET /api/transaction/transfer/");
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();


        } catch (Exception e) {
            log.error("Error in POST /transaction", e);
            throw e;
        }

        return ResponseEntity.ok(true);
    }
}
