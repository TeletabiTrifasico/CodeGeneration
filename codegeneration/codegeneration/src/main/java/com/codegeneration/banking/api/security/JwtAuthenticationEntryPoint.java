package com.codegeneration.banking.api.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * This class is used to return a 401 unauthorized error to clients that try to access
 * a protected resource without proper authentication.
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.error("Unauthorized access error: {}", authException.getMessage());

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                "Unauthorized: " + authException.getMessage());
    }
}