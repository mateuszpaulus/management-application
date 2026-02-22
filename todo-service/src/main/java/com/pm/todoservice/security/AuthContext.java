package com.pm.todoservice.security;

import java.util.UUID;

public record AuthContext(UUID userId, String role) {
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
