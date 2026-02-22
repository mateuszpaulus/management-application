package com.pm.authservice.dto.tokenDto;

public record TokenValidationResponseDTO(
        String userId,
        String email,
        String role
) {
}
