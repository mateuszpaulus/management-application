package com.pm.authservice.controller;

import com.pm.authservice.dto.UserDTO;
import com.pm.authservice.dto.loginDto.LoginRequestDTO;
import com.pm.authservice.dto.loginDto.LoginResponseDTO;
import com.pm.authservice.dto.registerDto.RegisterRequestDTO;
import com.pm.authservice.dto.registerDto.RegisterResponseDTO;
import com.pm.authservice.dto.tokenDto.TokenValidationResponseDTO;
import com.pm.authservice.service.AuthService;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Generate token on user login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO) {

        LoginResponseDTO response = authService.authenticate(loginRequestDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Validate Token")
    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponseDTO> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            TokenValidationResponseDTO response = authService.validateAndExtract(authHeader.substring(7));
            return ResponseEntity.ok(response);
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

    }

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO registerRequestDTO) {

        UserDTO createdUser = authService.register(registerRequestDTO);

        RegisterResponseDTO response = new RegisterResponseDTO(
                "Registration successful. You can now log in.",
                createdUser
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
