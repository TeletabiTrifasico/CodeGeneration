package com.codegeneration.banking.controllers;

import com.codegeneration.banking.api.dto.transaction.TransactionDTO;
import com.codegeneration.banking.api.dto.transaction.TransactionRequest;
import com.codegeneration.banking.api.dto.transaction.TransactionResponse;
import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.Transaction;
import com.codegeneration.banking.api.exception.ResourceNotFoundException;
import com.codegeneration.banking.api.security.JwtTokenProvider;
import com.codegeneration.banking.api.service.account.AccountService;
import com.codegeneration.banking.api.service.transaction.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;
    private final AccountService accountService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Get all transactions", description = "Returns all transactions belonging to the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/getall")
    public ResponseEntity<TransactionResponse> getAllTransactions(@Valid @RequestBody TransactionRequest request) {

        String username = jwtTokenProvider.getUsernameFromToken(request.getToken());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        List<Transaction> transactions = transactionService.getAllTransactionsByUsername(username);

        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());

        TransactionResponse response = TransactionResponse.builder()
                .transactions(transactionDTOs)
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get transactions by account number", description = "Returns all transactions for a specific account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid token or unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/byaccount")
    public ResponseEntity<TransactionResponse> getTransactionsByAccount(@Valid @RequestBody TransactionRequest request) {

        String username = jwtTokenProvider.getUsernameFromToken(request.getToken());

        Account account = accountService.getAccountByNumberAndUsername(request.getAccountNumber(), username);
        if (account == null) {
            throw new ResourceNotFoundException("Account not found with number: " + request.getAccountNumber());
        }

        List<Transaction> transactions = transactionService.getTransactionsByAccount(account);

        List<TransactionDTO> transactionDTOs = transactions.stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());

        TransactionResponse response = TransactionResponse.builder()
                .transactions(transactionDTOs)
                .build();

        return ResponseEntity.ok(response);
    }
}