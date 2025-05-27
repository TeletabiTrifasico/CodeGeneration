package com.codegeneration.banking.controllers;

import com.codegeneration.banking.api.dto.transaction.TransactionDTO;
import com.codegeneration.banking.api.dto.transactionfilter.TransactionFilterRequest;
import com.codegeneration.banking.api.dto.transaction.TransactionResponse;
import com.codegeneration.banking.api.dto.transaction.TransferRequest;
import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.Transaction;
import com.codegeneration.banking.api.exception.ResourceNotFoundException;
import com.codegeneration.banking.api.repository.AccountRepository;
import com.codegeneration.banking.api.repository.TransactionRepository;
import com.codegeneration.banking.api.service.interfaces.AccountService;
import com.codegeneration.banking.api.service.interfaces.TransactionFilterService;
import com.codegeneration.banking.api.service.interfaces.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "Transaction API")
@Slf4j
public class TransactionController extends BaseController {

    private final TransactionService transactionService;
    private final TransactionFilterService transactionFilterService;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

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

    @Operation(summary = "Get filtered transactions", description = "Returns filtered transactions belonging to the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered transactions",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/filter")
    public ResponseEntity<TransactionResponse> getFilteredTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal amountLessThan,
            @RequestParam(required = false) BigDecimal amountGreaterThan,
            @RequestParam(required = false) BigDecimal amountEqualTo,
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String transactionStatus,
            @RequestParam(required = false) String description) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authentication found for GET /transaction/filter");
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            log.info("Processing GET /transaction/filter for user: {}", username);

            TransactionFilterRequest filterRequest = TransactionFilterRequest.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .amountLessThan(amountLessThan)
                    .amountGreaterThan(amountGreaterThan)
                    .amountEqualTo(amountEqualTo)
                    .iban(iban)
                    .transactionType(transactionType)
                    .transactionStatus(transactionStatus)
                    .description(description)
                    .build();

            List<Transaction> transactions = transactionFilterService.getFilteredTransactionsByUsername(username, filterRequest);

            List<TransactionDTO> transactionDTOs = transactions.stream()
                    .map(TransactionDTO::fromEntity)
                    .collect(Collectors.toList());

            TransactionResponse response = TransactionResponse.builder()
                    .transactions(transactionDTOs)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in GET /transaction/filter", e);
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

    @Operation(summary = "Get filtered transactions by account", description = "Returns filtered transactions for a specific account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered transactions",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/byaccount/{accountNumber}/filter")
    public ResponseEntity<TransactionResponse> getFilteredTransactionsByAccount(
            @PathVariable String accountNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal amountLessThan,
            @RequestParam(required = false) BigDecimal amountGreaterThan,
            @RequestParam(required = false) BigDecimal amountEqualTo,
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String transactionStatus,
            @RequestParam(required = false) String description) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authentication found for GET /transaction/byaccount/{}/filter", accountNumber);
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            log.info("Processing GET /transaction/byaccount/{}/filter for user: {}", accountNumber, username);

            TransactionFilterRequest filterRequest = TransactionFilterRequest.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .amountLessThan(amountLessThan)
                    .amountGreaterThan(amountGreaterThan)
                    .amountEqualTo(amountEqualTo)
                    .iban(iban)
                    .transactionType(transactionType)
                    .transactionStatus(transactionStatus)
                    .description(description)
                    .build();

            List<Transaction> transactions = transactionFilterService.getFilteredTransactionsByAccount(accountNumber, username, filterRequest);

            List<TransactionDTO> transactionDTOs = transactions.stream()
                    .map(TransactionDTO::fromEntity)
                    .collect(Collectors.toList());

            TransactionResponse response = TransactionResponse.builder()
                    .transactions(transactionDTOs)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in GET /transaction/byaccount/{}/filter", e);
            throw e;
        }
    }

    @Operation(summary = "Transfer money between accounts", description = "Process a money transfer between accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer completed successfully",
                    content = @Content(schema = @Schema(implementation = TransactionDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "422", description = "Insufficient funds or limit exceeded"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/transfer")
    public ResponseEntity<TransactionDTO> transferMoney(@RequestBody TransferRequest transferRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authentication found for POST /api/transaction/transfer");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String username = authentication.getName();
            log.info("Processing POST /api/transaction/transfer for user: {}", username);

            // Validate request parameters
            if (transferRequest.getFromAccount() == null || transferRequest.getToAccount() == null ||
                    transferRequest.getAmount() == null) {
                return ResponseEntity.badRequest().build();
            }

            // Validate source account belongs to the authenticated user
            Account sourceAccount = accountService.getAccountByNumberAndUsername(
                    transferRequest.getFromAccount(), username);

            if (sourceAccount == null) {
                throw new ResourceNotFoundException("Source account not found or does not belong to you: "
                        + transferRequest.getFromAccount());
            }

            // Validate amount is positive
            if (transferRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().build();
            }

            // Validate sufficient funds
            if (sourceAccount.getBalance().compareTo(transferRequest.getAmount()) < 0) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body(null); // 422 for insufficient funds
            }

            // Validate transfer limits
            if (!sourceAccount.isTransferAllowed(transferRequest.getAmount())) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body(null); // 422 for limit exceeded
            }

            // Get destination account (no need to belong to the authenticated user)
            Account destinationAccount = accountService.getAccountByNumber(transferRequest.getToAccount());
            if (destinationAccount == null) {
                throw new ResourceNotFoundException("Destination account not found: " + transferRequest.getToAccount());
            }

            // Generate a unique transaction reference
            String transactionReference = generateTransactionReference();

            // Create the transaction
            Transaction transaction = Transaction.builder()
                    .transactionReference(transactionReference)
                    .sourceAccount(sourceAccount)
                    .destinationAccount(destinationAccount)
                    .amount(transferRequest.getAmount())
                    .description(transferRequest.getDescription() != null ?
                            transferRequest.getDescription() : "Transfer")
                    .currency(sourceAccount.getCurrency())
                    .status(Transaction.TransactionStatus.PENDING)
                    .type(Transaction.TransactionType.TRANSFER)
                    .createdAt(LocalDateTime.now())
                    .build();

            // Update account balances
            sourceAccount.setBalance(sourceAccount.getBalance().subtract(transferRequest.getAmount()));
            sourceAccount.updateTransferUsed(transferRequest.getAmount());

            destinationAccount.setBalance(destinationAccount.getBalance().add(transferRequest.getAmount()));

            // Save transaction and updated accounts
            Transaction savedTransaction = transactionRepository.save(transaction);
            accountRepository.save(sourceAccount);
            accountRepository.save(destinationAccount);

            // Mark as completed after successful processing
            savedTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            savedTransaction.setCompletedAt(LocalDateTime.now());
            savedTransaction = transactionRepository.save(savedTransaction);

            return ResponseEntity.ok(TransactionDTO.fromEntity(savedTransaction));

        } catch (ResourceNotFoundException e) {
            log.error("Resource not found in transfer: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in POST /api/transaction/transfer", e);
            throw e;
        }
    }

    /**
     * Generate a unique transaction reference
     *
     * @return A unique transaction reference string
     */
    private String generateTransactionReference() {
        return "TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}