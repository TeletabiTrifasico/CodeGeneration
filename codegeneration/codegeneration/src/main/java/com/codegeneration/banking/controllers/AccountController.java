package com.codegeneration.banking.controllers;

import com.codegeneration.banking.api.dto.UsernameRequest;
import com.codegeneration.banking.api.dto.LimitUpdateRequest;
import com.codegeneration.banking.api.dto.account.AccountDTO;
import com.codegeneration.banking.api.dto.account.AccountResponse;
import com.codegeneration.banking.api.dto.account.CreateAccountRequest;
import com.codegeneration.banking.api.dto.account.CreateAccountResponse;
import com.codegeneration.banking.api.dto.transaction.TransactionRequest;
import com.codegeneration.banking.api.entity.Account;
import com.codegeneration.banking.api.entity.User;
import com.codegeneration.banking.api.repository.AccountRepository;
import com.codegeneration.banking.api.repository.UserRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

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

        Account account = null;
        // First try to get account if it belongs to the user
        try {
            account = accountService.getAccountByNumberAndUsername(accountNumber, username);
        }
        catch (Exception e) {
            log.error("Target account not owned by user");
        }

        // If not found, try to get account details without ownership check (for transfers)
        if (account == null) {
            account = accountService.getAccountByNumber(accountNumber);
            if (account == null) {
                return ResponseEntity.notFound().build();
            }
        }

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

    @Operation(summary = "Search for accounts by username", description = "Returns accounts matching the search term, including user's own accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully searched accounts",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<AccountResponse> searchAccounts(@RequestParam("term") String searchTerm) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authentication found for GET /account/search");
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            log.info("Processing GET /account/search with term: '{}' for user: {}", searchTerm, username);

            // Ensure search term is not empty and has minimum length
            if (searchTerm == null || searchTerm.trim().length() < 2) {
                return ResponseEntity.ok(new AccountResponse(List.of()));
            }

            // Find accounts by username search
            List<Account> accounts = accountService.searchAccountsByUsername(searchTerm.trim());

            // Get current user's accounts to include them in results
            List<Account> currentUserAccounts = accountService.getAccountsByUsername(username);

            // Combine results - include user's own accounts if they match the search term
            List<Account> allResults = new ArrayList<>(accounts);

            // Add user's own accounts if the search term matches their username or name
            String searchTermLower = searchTerm.trim().toLowerCase();
            if (username.toLowerCase().contains(searchTermLower) ||
                    (currentUserAccounts.size() > 0 &&
                            currentUserAccounts.get(0).getUser().getName().toLowerCase().contains(searchTermLower))) {

                // Add user's own accounts that aren't already in the results
                for (Account userAccount : currentUserAccounts) {
                    boolean alreadyExists = allResults.stream()
                            .anyMatch(acc -> acc.getAccountNumber().equals(userAccount.getAccountNumber()));
                    if (!alreadyExists) {
                        allResults.add(userAccount);
                    }
                }
            }

            List<AccountDTO> accountDTOs = allResults.stream()
                    .map(AccountDTO::fromEntity)
                    .collect(Collectors.toList());

            AccountResponse response = new AccountResponse(accountDTOs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in GET /account/search", e);
            throw e;
        }
    }

    @Operation(summary = "Create new account", description = "Creates a new account for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully",
                    content = @Content(schema = @Schema(implementation = CreateAccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CreateAccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        try {
            // Get username from authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authentication found for POST /account/create");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String username = authentication.getName();
            log.info("Processing POST /account/create for user: {}", username);

            // Find the user
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isEmpty()) {
                log.error("User not found: {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User user = userOptional.get();

            // Create the account
            Account createdAccount = accountService.createAccount(request, user);

            // Build response
            CreateAccountResponse response = CreateAccountResponse.builder()
                    .accountNumber(createdAccount.getAccountNumber())
                    .accountName(createdAccount.getAccountName())
                    .accountType(createdAccount.getAccountType())
                    .currency(createdAccount.getCurrency())
                    .balance(createdAccount.getBalance())
                    .dailyTransferLimit(createdAccount.getDailyTransferLimit())
                    .createdAt(createdAccount.getCreatedAt())
                    .ownerUsername(user.getUsername())
                    .build();

            log.info("Account created successfully: {} for user: {}", 
                    createdAccount.getAccountNumber(), username);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error creating account for user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Edit limits for an account", description = "Edits the different limit fields for an account")
    @PutMapping("/limits")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<String>> updateAccountLimits(@Valid @RequestBody LimitUpdateRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getAuthorities().stream().anyMatch(auth -> "ROLE_EMPLOYEE".equals(auth.getAuthority()))) {
            String accountNumber = request.getAccountNumber();
            Account account = accountService.getAccountByNumber(accountNumber);
            //Could be moved to account entity possibly to look cleaner
            account.setDailyTransferLimit(BigDecimal.valueOf(request.getDailyTransferLimit()));
            account.setSingleTransferLimit(BigDecimal.valueOf(request.getSingleTransferLimit()));
            account.setDailyWithdrawalLimit(BigDecimal.valueOf(request.getDailyWithdrawalLimit()));
            account.setSingleWithdrawalLimit(BigDecimal.valueOf(request.getSingleWithdrawalLimit()));
            accountRepository.save(account);
            List<String> response = new ArrayList<>();
            response.add("Successfully edited limits for account: " + accountNumber);
            log.info(response.toString());
            return ResponseEntity.ok(response);
        }
        else {
            //not an employee
            return ResponseEntity.status(401).build();
        }
    }
}