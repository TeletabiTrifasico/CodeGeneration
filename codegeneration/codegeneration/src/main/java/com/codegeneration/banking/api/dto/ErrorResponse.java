package com.codegeneration.banking.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Error response data")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "401")
    private int status;

    @Schema(description = "Error message", example = "Invalid credentials")
    private String message;

    @Schema(description = "Timestamp", example = "2025-04-19T12:34:56")
    private LocalDateTime timestamp;

    @Schema(description = "Request path", example = "/api/auth/login")
    private String path;
}
