package com.pm.authservice.grpc;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.UserDTO;
import com.pm.grpc.user.*;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserServiceGrpcClient {
    private static final Logger log = LoggerFactory.getLogger(UserServiceGrpcClient.class);

    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    public UserDTO validateCredentials(LoginRequestDTO loginRequestDTO) {
        try {
            log.debug("Validating credentials for user: {}", loginRequestDTO.getEmail());

            ValidateCredentialsRequest request = ValidateCredentialsRequest.newBuilder()
                    .setEmail(loginRequestDTO.getEmail())
                    .setPassword(loginRequestDTO.getPassword())
                    .build();

            ValidateCredentialsResponse response = userServiceStub.validateCredentials(request);

            if (response.getSuccess()) {
                log.info("Credentials validated successfully for user: {}", loginRequestDTO.getEmail());
                UserData userData = response.getUser();

                return UserDTO.fromUserData(userData);
            } else {
                log.warn("Invalid credentials for user: {}", loginRequestDTO.getEmail());
                return null;
            }

        } catch (StatusRuntimeException e) {
            log.error("gRPC error while validating credentials: {}", e.getMessage());
            throw new RuntimeException("Failed to communicate with user-service", e);
        }
    }

    public UserDTO getUserById(String userId) {
        try {
            log.debug("Fetching user by ID: {}", userId);

            GetUserByIdRequest request = GetUserByIdRequest.newBuilder()
                    .setUserId(userId)
                    .build();

            UserResponse response = userServiceStub.getUserById(request);

            if (response.getSuccess()) {
                UserData userData = response.getUser();

                return UserDTO.fromUserData(userData);

            } else {
                log.warn("User not found with ID: {}", userId);
                return null;
            }

        } catch (StatusRuntimeException e) {
            log.error("gRPC error while fetching user: {}", e.getMessage());
            throw new RuntimeException("Failed to communicate with user-service", e);
        }
    }
}