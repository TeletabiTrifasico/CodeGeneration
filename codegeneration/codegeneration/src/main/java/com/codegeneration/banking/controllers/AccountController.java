package com.codegeneration.banking.controllers;

import com.codegeneration.banking.api.dto.account.AccountDTO;
import com.codegeneration.banking.api.dto.account.AccountResponse;
import com.codegeneration.banking.api.dto.transaction.TransactionRequest;
import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.security.JwtTokenProvider;
import com.codegeneration.banking.api.service.account.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Tag(name = "Account", description = "Account API")
@CrossOrigin(origins = "*")
public class AccountController {

    private final AccountService accountService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Get all accounts", description = "Returns all accounts belonging to the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved accounts",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/getall")
    public ResponseEntity<AccountResponse> getAllAccounts(@Valid @RequestBody TransactionRequest request) {

        String username = jwtTokenProvider.getUsernameFromToken(request.getToken());

        List<Account> accounts = accountService.getAccountsByUsername(username);

        List<AccountDTO> accountDTOs = accounts.stream()
                .map(AccountDTO::fromEntity)
                .collect(Collectors.toList());

        AccountResponse response = AccountResponse.builder()
                .accounts(accountDTOs)
                .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get account details", description = "Returns details for a specific account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved account details",
                    content = @Content(schema = @Schema(implementation = AccountDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid token or unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/details")
    public ResponseEntity<AccountDTO> getAccountDetails(@Valid @RequestBody TransactionRequest request) {

        String username = jwtTokenProvider.getUsernameFromToken(request.getToken());

        Account account = accountService.getAccountByNumberAndUsername(
                request.getAccountNumber(), username);

        AccountDTO accountDTO = AccountDTO.fromEntity(account);

        return ResponseEntity.ok(accountDTO);
    }
}