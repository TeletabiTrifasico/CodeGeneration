package com.codegeneration.banking.api.service.tokenblacklistservice;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklistService {
    // Store blacklisted tokens with their expiration time
    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    /**
     * Add a token to the blacklist
     *
     * @param token JWT token
     * @param expiryDate Token expiration date
     */
    public void blacklistToken(String token, Instant expiryDate) {
        blacklistedTokens.put(token, expiryDate);
    }

    /**
     * Check if a token is blacklisted
     *
     * @param token JWT token
     * @return True if token is blacklisted
     */
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    /**
     * Clean up expired tokens from the blacklist
     * This method runs every hour
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
