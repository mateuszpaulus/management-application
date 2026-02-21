package com.pm.authservice.dto.errorDto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class ValidationErrorResponseDTO {
    private String message;
    private Map<String, String> errors;

    public ValidationErrorResponseDTO(String message, Map<String, String> errors) {
        this.message = message;
        this.errors = errors;
    }
}