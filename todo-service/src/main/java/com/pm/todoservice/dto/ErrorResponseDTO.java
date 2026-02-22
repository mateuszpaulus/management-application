package com.pm.todoservice.dto;

import java.time.Instant;

public record ErrorResponseDTO(
        int status,
        String error,
        String message,
        Instant timestamp
) {
}
