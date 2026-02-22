package com.pm.apigateway.dto;

public record TokenValidationResponse(
        String userId,
        String email,
        String role
) {
}
