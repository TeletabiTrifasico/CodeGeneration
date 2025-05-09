package com.codegeneration.banking.controllers;

import com.codegeneration.banking.api.dto.UsernameRequest;
import com.codegeneration.banking.api.dto.account.AccountDTO;
import com.codegeneration.banking.api.dto.account.AccountResponse;
import com.codegeneration.banking.api.dto.transaction.TransactionRequest;
import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.security.JwtAuthenticationFilter;
import com.codegeneration.banking.api.security.JwtTokenProvider;
import com.codegeneration.banking.api.service.interfaces.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Tag(name = "Account", description = "Account API")
@Slf4j
public class AccountController extends BaseController {

    private final AccountService accountService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Operation(summary = "Get all accounts", description = "Returns all accounts belonging to the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/getall")
    public ResponseEntity<AccountResponse> getAllAccounts() {
        try {
            // Get username from authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authentication found for GET /account/getall");
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            log.info("Processing GET /account/getall for user: {}", username);

            List<Account> accounts = accountService.getAccountsByUsername(username);

            List<AccountDTO> accountDTOs = accounts.stream()
                    .map(AccountDTO::fromEntity)
                    .collect(Collectors.toList());

            AccountResponse response = AccountResponse.builder()
                    .accounts(accountDTOs)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in GET /account/getall", e);
            throw e;
        }
    }

    @Operation(summary = "Get account details", description = "Returns details for a specific account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved account details",
                    content = @Content(schema = @Schema(implementation = AccountDTO.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/details/{accountNumber}")
    public ResponseEntity<AccountDTO> getAccountDetails(@PathVariable String accountNumber) {
        // Get username from authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authentication found for GET /account/details/{}", accountNumber);
            return ResponseEntity.status(401).build();
        }

        String username = authentication.getName();
        log.info("Processing GET /account/details/{} for user: {}", accountNumber, username);

        Account account = accountService.getAccountByNumberAndUsername(accountNumber, username);
        AccountDTO accountDTO = AccountDTO.fromEntity(account);

        return ResponseEntity.ok(accountDTO);
    }

    @Operation(summary = "Get specific account IBAN by username", description = "Returns the IBAN numbers for a given user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved IBANs",
                    content = @Content(array = @ArraySchema(schema = @Schema(type = "string")))),
            @ApiResponse(responseCode = "401", description = "Invalid username"),
            @ApiResponse(responseCode = "500", description = "Internal server error!!")
    })
    @PostMapping("/getIBANByUsername")
    public ResponseEntity<List<String>> getIBANByUsername(@Valid @RequestBody UsernameRequest request) {
        List<String> response = new ArrayList<>();
        String username = request.getUsername();
        List<Account> accounts = accountService.getAccountsByUsername(username);
        for (Account account : accounts) {
            response.add(account.getAccountNumber());
        }
        return ResponseEntity.ok(response);
    }

}