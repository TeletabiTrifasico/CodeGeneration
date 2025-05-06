package com.codegeneration.banking.controllers;

import com.codegeneration.banking.api.dto.LoginRequest;
import com.codegeneration.banking.api.dto.LoginResponse;
import com.codegeneration.banking.api.dto.UserDTO;
import com.codegeneration.banking.api.dto.logout.LogoutRequest;
import com.codegeneration.banking.api.dto.transaction.TransactionRequest;
import com.codegeneration.banking.api.dto.RegisterRequest;
import com.codegeneration.banking.api.service.AuthService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API")
@CrossOrigin(origins = "*") // Restrict in production
public class AuthController {

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
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, @RequestBody(required = false) LogoutRequest logoutRequest) {
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

    @Operation(summary = "Validate token", description = "Validates a JWT token and returns user information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Valid token",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid token")
    })
    @PostMapping("/validate")
    public ResponseEntity<UserDTO> validateToken(@Valid @RequestBody TransactionRequest request) {
        String token = request.getToken();
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDTO userDTO = authService.validateToken(token);
        if (userDTO == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(userDTO);
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