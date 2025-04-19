package com.codegeneration.banking.api.config;

import com.codegeneration.banking.api.entity.User;
import com.codegeneration.banking.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Check if users already exist
        if (userRepository.count() == 0) {
            loadUsers();
        }
    }

    private void loadUsers() {
        log.info("Loading sample users...");

        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .name("Admin User")
                .email("admin@example.com")
                .roles(Arrays.asList("ROLE_ADMIN", "ROLE_USER"))
                .enabled(true)
                .build();

        User user = User.builder()
                .username("user")
                .password(passwordEncoder.encode("user123"))
                .name("Regular User")
                .email("user@example.com")
                .roles(List.of("ROLE_USER"))
                .enabled(true)
                .build();

        userRepository.saveAll(Arrays.asList(admin, user));

        log.info("Sample users loaded successfully");
    }
}