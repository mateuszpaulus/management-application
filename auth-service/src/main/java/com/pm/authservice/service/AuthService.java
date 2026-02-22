package com.pm.authservice.service;

import com.pm.authservice.dto.*;
import com.pm.authservice.dto.loginDto.LoginRequestDTO;
import com.pm.authservice.dto.loginDto.LoginResponseDTO;
import com.pm.authservice.dto.registerDto.RegisterRequestDTO;
import com.pm.authservice.dto.tokenDto.TokenValidationResponseDTO;
import com.pm.authservice.grpc.UserServiceGrpcClient;
import com.pm.authservice.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserServiceGrpcClient userGrpcClient;
    private final JwtUtil jwtUtil;

    public AuthService(UserServiceGrpcClient userGrpcClient, JwtUtil jwtUtil) {
        this.userGrpcClient = userGrpcClient;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponseDTO authenticate(LoginRequestDTO loginRequestDTO) {
        UserDTO user = userGrpcClient.validateCredentials(loginRequestDTO);

        String token = jwtUtil.generateToken(
                user.getId().toString(),
                user.getEmail(),
                user.getRole()
        );

        return new LoginResponseDTO(token, user);
    }

    public TokenValidationResponseDTO validateAndExtract(String token) {
        Claims claims = jwtUtil.extractClaims(token);
        return new TokenValidationResponseDTO(
                claims.get("userId", String.class),
                claims.getSubject(),
                claims.get("role", String.class)
        );
    }

    public UserDTO register(RegisterRequestDTO registerRequestDTO) {
        return userGrpcClient.createUser(registerRequestDTO);
    }
}
