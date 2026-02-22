package com.pm.authservice.dto;

import com.pm.grpc.user.UserData;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
public class UserDTO {
    private UUID id;
    private String username;
    private String email;
    private String role;

    public UserDTO(UUID id, String username, String email, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    public static UserDTO fromUserData(UserData userData) {
        return new UserDTO(
                UUID.fromString(userData.getId()),
                userData.getUsername(),
                userData.getEmail(),
                userData.getRole()
        );
    }
}