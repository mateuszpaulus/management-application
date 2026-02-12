package com.pm.userservice.dto;

import com.pm.userservice.model.enums.UserRole;
import lombok.*;


import java.util.UUID;

@Data
@NoArgsConstructor
public class UserResponseDTO {

    private UUID id;
    private String username;
    private String email;
    private UserRole role;

    public UserResponseDTO(UUID id, String username, String email, UserRole role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}