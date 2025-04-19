package com.codegeneration.banking.api.service.impl;

import com.codegeneration.banking.api.dto.LoginRequest;
import com.codegeneration.banking.api.dto.LoginResponse;
import com.codegeneration.banking.api.dto.UserDTO;
import com.codegeneration.banking.api.entity.User;
import com.codegeneration.banking.api.exception.UnauthorizedException;
import com.codegeneration.banking.api.repository.UserRepository;
import com.codegeneration.banking.api.security.JwtTokenProvider;
import com.codegeneration.banking.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            org.springframework.security.core.userdetails.User userDetails =
                    (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            String token = jwtTokenProvider.generateToken(authentication);

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            UserDTO userDTO = UserDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .name(user.getName())
                    .email(user.getEmail())
                    .roles(roles)
                    .build();

            return LoginResponse.builder()
                    .token(token)
                    .user(userDTO)
                    .expiresIn(jwtTokenProvider.getExpirationTime())
                    .build();
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    @Override
    public UserDTO validateToken(String token) {
        // Validate token
        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Invalid token");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        List<String> roles = jwtTokenProvider.getRolesFromToken(token);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }
}