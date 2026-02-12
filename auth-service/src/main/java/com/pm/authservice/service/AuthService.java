package com.pm.authservice.service;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.LoginResponseDTO;
import com.pm.authservice.dto.UserDTO;
import com.pm.authservice.grpc.UserServiceGrpcClient;
import com.pm.authservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.BadCredentialsException;
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
        if (user == null) {
            throw new BadCredentialsException("Invalid username or password");
        }

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

}
