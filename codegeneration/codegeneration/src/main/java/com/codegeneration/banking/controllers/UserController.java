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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
}