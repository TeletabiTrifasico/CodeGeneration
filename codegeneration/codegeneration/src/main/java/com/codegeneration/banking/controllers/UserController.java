package com.codegeneration.banking.controllers;

import com.codegeneration.banking.api.service.interfaces.AuthService;
import com.codegeneration.banking.api.service.implementations.UserServiceImpl;
import com.codegeneration.banking.api.dto.user.UserResponse;
import com.codegeneration.banking.api.dto.user.UserDTO;
import com.codegeneration.banking.api.entity.User;
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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Users API")
@Slf4j
public class UserController extends BaseController {

    private final AuthService authService;
    private final UserServiceImpl userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @GetMapping("/getall")
    public ResponseEntity<UserResponse> getAllUsers() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authentication found for GET /transaction/getall");
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            log.info("Processing GET /users/getall for user: {}", username);

            List<User> users = userService.getAllUsers();
            List<UserDTO> userDTOs = users.stream()
                    .map(UserDTO::fromEntity)
                    .collect(Collectors.toList());

            UserResponse response = UserResponse.builder()
                    .users(userDTOs)
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in GET /users/getall", e);
            throw e;
        }
    }
    @GetMapping("")
    public ResponseEntity<?> getUsersByPage(@RequestParam Number limit, @RequestParam Number page) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authentication found for GET /transaction/getall");
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            log.info("Processing GET /users/getall for user: {}", username);
            try {
                List<UserDTO> userDTOs = userService.getUsersByPage(page, limit).stream()
                    .map(UserDTO::fromEntity)
                    .collect(Collectors.toList());
                
                UserResponse response = UserResponse.builder()
                    .users(userDTOs)
                    .build();

                return ResponseEntity.ok(response);
            }
            catch(ResponseStatusException e) {
                log.info(e.getMessage());
                log.info(String.valueOf(e.getStatusCode().value()));
                //HttpStatusCode test = e.getStatusCode().value();
                return ResponseEntity.status(e.getStatusCode().value()).body(e.getReason());
            }
        } catch (Exception e) {
            log.error("Error in GET /users", e);
            throw e;
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Number id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authentication found for GET /transaction/getall");
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            log.info("Processing GET /users/getall for user: {}", username);
            try {
                List<UserDTO> userDTO = userService.getUserById(id).stream()
                        .map(UserDTO::fromEntity)
                        .collect(Collectors.toList());

                UserResponse response = UserResponse.builder()
                        .users(userDTO)
                        .build();

                return ResponseEntity.ok(response);
            }
            catch(ResponseStatusException e) {
                log.info(e.getMessage());
                log.info(String.valueOf(e.getStatusCode().value()));
                //HttpStatusCode test = e.getStatusCode().value();
                return ResponseEntity.status(e.getStatusCode().value()).body(e.getReason());
            }
        } catch (Exception e) {
            log.error("Error in GET /users", e);
            throw e;
        }
    }

    @Operation(summary = "Get disabled users (pending approval)", description = "Retrieves a paginated list of users that are disabled and pending employee approval. Only employees can access this endpoint.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved disabled users",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - employee role required"),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/disabled")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDisabledUsers(@RequestParam Number limit, @RequestParam Number page) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authentication found for GET /users/disabled");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Check if user has EMPLOYEE role
            if (!authentication.getAuthorities().stream().anyMatch(auth -> "ROLE_EMPLOYEE".equals(auth.getAuthority()))) {
                log.warn("Non-employee user attempted to access disabled users list");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            String username = authentication.getName();
            log.info("Processing GET /users/disabled for employee: {}", username);

            try {
                List<UserDTO> disabledUserDTOs = userService.getDisabledUsersByPage(page, limit).stream()
                        .map(UserDTO::fromEntity)
                        .collect(Collectors.toList());

                UserResponse response = UserResponse.builder()
                        .users(disabledUserDTOs)
                        .build();

                return ResponseEntity.ok(response);
            } catch (ResponseStatusException e) {
                log.info("Error retrieving disabled users: {}", e.getMessage());
                return ResponseEntity.status(e.getStatusCode().value()).body(e.getReason());
            }
        } catch (Exception e) {
            log.error("Error in GET /users/disabled", e);
            throw e;
        }
    }

    @Operation(summary = "Enable a user account", description = "Enables a disabled user account, allowing them to log in. Only employees can perform this action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User account successfully enabled",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - employee role required"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Bad request - user already enabled"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{userId}/enable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> enableUser(@PathVariable Long userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("No authentication found for PUT /users/{}/enable", userId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Check if user has EMPLOYEE role
            if (!authentication.getAuthorities().stream().anyMatch(auth -> "ROLE_EMPLOYEE".equals(auth.getAuthority()))) {
                log.warn("Non-employee user attempted to enable user with ID: {}", userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            String username = authentication.getName();
            log.info("Processing PUT /users/{}/enable for employee: {}", userId, username);

            try {
                User enabledUser = userService.enableUser(userId);
                UserDTO userDTO = UserDTO.fromEntity(enabledUser);

                log.info("Employee {} successfully enabled user with ID: {}", username, userId);
                return ResponseEntity.ok(userDTO);
            } catch (ResponseStatusException e) {
                log.info("Error enabling user with ID {}: {}", userId, e.getMessage());
                return ResponseEntity.status(e.getStatusCode().value()).body(e.getReason());
            }
        } catch (Exception e) {
            log.error("Error in PUT /users/{}/enable", userId, e);
            throw e;
        }
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> filterUsers(
            @RequestParam(defaultValue = "10") Number limit,
            @RequestParam(defaultValue = "1") Number page,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer minAccounts,
            @RequestParam(required = false) BigDecimal minBalance,
            @RequestParam(required = false) BigDecimal maxBalance) {
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            boolean enabled = status == null ? true : Boolean.parseBoolean(status);
            
            List<User> filteredUsers = userService.filterUsers(
                name, username, email, role, enabled, minAccounts, 
                minBalance, maxBalance, page.intValue(), limit.intValue()
            );
            
            List<UserDTO> userDTOs = filteredUsers.stream()
                    .map(UserDTO::fromEntity)
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            log.error("Error filtering users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error filtering users: " + e.getMessage());
        }
    }
}