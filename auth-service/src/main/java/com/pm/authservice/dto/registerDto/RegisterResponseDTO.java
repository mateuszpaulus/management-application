package com.pm.authservice.dto.registerDto;

import com.pm.authservice.dto.UserDTO;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterResponseDTO {

    private String message;
    private UserDTO user;

    public RegisterResponseDTO(String message, UserDTO user) {
        this.message = message;
        this.user = user;
    }

}