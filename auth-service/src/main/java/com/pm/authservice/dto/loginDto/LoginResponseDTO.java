package com.pm.authservice.dto.loginDto;

import com.pm.authservice.dto.UserDTO;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class LoginResponseDTO {
    private String token;
    private UserDTO user;

    public LoginResponseDTO(String token, UserDTO user) {
        this.token = token;
        this.user = user;
    }

}