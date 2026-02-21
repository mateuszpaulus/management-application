package com.pm.authservice.service;

import com.pm.authservice.dto.*;
import com.pm.authservice.dto.loginDto.LoginRequestDTO;
import com.pm.authservice.dto.loginDto.LoginResponseDTO;
import com.pm.authservice.dto.registerDto.RegisterRequestDTO;
import com.pm.authservice.grpc.UserServiceGrpcClient;
import com.pm.authservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
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

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());

        return new LoginResponseDTO(token, user);
    }

    public boolean validateToken(String token) {
        try {
            jwtUtil.validateToken(token);
            return true;

        } catch (JwtException e) {
            return false;
        }
    }

    public UserDTO register(RegisterRequestDTO registerRequestDTO) {
        return userGrpcClient.createUser(registerRequestDTO);
    }
}
