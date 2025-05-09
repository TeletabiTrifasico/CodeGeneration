package com.codegeneration.banking.controllers;

import com.codegeneration.banking.api.dto.login.LoginRequest;
import com.codegeneration.banking.api.dto.login.LoginResponse;
import com.codegeneration.banking.api.dto.login.UserDTO;
import com.codegeneration.banking.api.dto.logout.LogoutRequest;
import com.codegeneration.banking.api.dto.transaction.TransactionRequest;
import com.codegeneration.banking.api.dto.register.RegisterRequest;
import com.codegeneration.banking.api.service.interfaces.AuthService;
import com.codegeneration.banking.api.security.JwtAuthenticationFilter;
import com.codegeneration.banking.api.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API")
@Slf4j
public class AuthController extends BaseController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Operation(summary = "Login a user", description = "Authenticates a user and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Operation(summary = "Logout a user", description = "Logs out a user by invalidating their JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully logged out"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request,
                                                      @RequestBody(required = false) LogoutRequest logoutRequest) {
        String token;
        if (logoutRequest != null && logoutRequest.getToken() != null) {
            token = logoutRequest.getToken();
        } else {
            token = jwtAuthenticationFilter.getJwtFromRequest(request);
        }

        if (token != null) {
            jwtTokenProvider.blacklistToken(token);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Validate token (GET method)", description = "Validates a JWT token from Authorization header and returns user information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid token",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    @GetMapping("/validate")
    public ResponseEntity<UserDTO> validateTokenGet(HttpServletRequest request) {
        try {
            // Get token from Authorization header
            String token = jwtAuthenticationFilter.getJwtFromRequest(request);
            if (token == null || token.isEmpty()) {
                log.warn("Missing token in GET /auth/validate");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Get authentication from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authentication in GET /auth/validate");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            UserDTO userDTO = authService.validateToken(token);
            if (userDTO == null) {
                log.warn("Invalid token in GET /auth/validate");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            log.error("Error in GET /auth/validate", e);
            throw e;
        }
    }

    @Operation(summary = "Validate token (POST method)", description = "Validates a JWT token and returns user information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid token",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    @PostMapping("/validate")
    public ResponseEntity<UserDTO> validateToken(@Valid @RequestBody(required = false) TransactionRequest request,
                                                 HttpServletRequest httpRequest) {
        try {
            String token;

            // If request is null or token is null, try getting token from header
            if (request == null || request.getToken() == null) {
                token = jwtAuthenticationFilter.getJwtFromRequest(httpRequest);
            } else {
                token = request.getToken();
            }

            if (token == null || token.isEmpty()) {
                log.warn("Missing token in POST /auth/validate");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            UserDTO userDTO = authService.validateToken(token);
            if (userDTO == null) {
                log.warn("Invalid token in POST /auth/validate");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            log.error("Error in POST /auth/validate", e);
            throw e;
        }
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data (validation failed)"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO newUserDto = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUserDto);
    }
}