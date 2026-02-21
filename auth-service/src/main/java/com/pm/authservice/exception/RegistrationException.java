package com.pm.authservice.exception;

public class RegistrationException extends RuntimeException {
    public RegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}