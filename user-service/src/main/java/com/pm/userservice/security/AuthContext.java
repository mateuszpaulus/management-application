package com.pm.userservice.security;

import java.util.UUID;

public record AuthContext(UUID userId, String role) {
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
