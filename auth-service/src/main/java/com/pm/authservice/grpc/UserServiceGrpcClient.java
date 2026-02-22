package com.pm.authservice.grpc;

import com.pm.authservice.dto.loginDto.LoginRequestDTO;
import com.pm.authservice.dto.registerDto.RegisterRequestDTO;
import com.pm.authservice.dto.UserDTO;
import com.pm.authservice.exception.InvalidCredentialsException;
import com.pm.authservice.exception.RegistrationException;
import com.pm.authservice.exception.UserAlreadyExistsException;
import com.pm.grpc.user.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.pm.grpc.user.UserServiceGrpc.UserServiceBlockingStub;
import com.pm.grpc.user.CreateUserRequest;
import com.pm.grpc.user.CreateUserResponse;

@Service
public class UserServiceGrpcClient {
    private static final Logger log = LoggerFactory.getLogger(UserServiceGrpcClient.class);

    //    @GrpcClient("user-service")
    private final UserServiceBlockingStub userServiceStub;

    public UserServiceGrpcClient(
            @Value("${user.service.address:localhost}") String serverAddress,
            @Value("${user.service.grpc.port:9001}") int serverPort
    ) {

        log.info("Connecting to User Service at {}:{}", serverAddress, serverPort);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress,
                serverPort).usePlaintext().build();

        userServiceStub = UserServiceGrpc.newBlockingStub(channel);
    }

    public UserDTO validateCredentials(LoginRequestDTO loginRequestDTO) {
        try {
            log.debug("Validating credentials for user: {}", loginRequestDTO.getEmail());

            ValidateCredentialsRequest request = ValidateCredentialsRequest.newBuilder()
                    .setEmail(loginRequestDTO.getEmail())
                    .setPassword(loginRequestDTO.getPassword())
                    .build();

            ValidateCredentialsResponse response = userServiceStub.validateCredentials(request);

            log.info("Credentials validated successfully for user: {}", loginRequestDTO.getEmail());
            log.info("hasUser={}, userId='{}'", response.hasUser(), response.getUser().getId());

            return UserDTO.fromUserData(response.getUser());

        } catch (StatusRuntimeException e) {
            log.error("gRPC error while validating credentials: {} - {}",
                    e.getStatus().getCode(),
                    e.getStatus().getDescription());

            switch (e.getStatus().getCode()) {
                case UNAUTHENTICATED:
                    throw new InvalidCredentialsException("Invalid email or password");
                case INVALID_ARGUMENT:
                    throw new IllegalArgumentException(e.getStatus().getDescription());
                case INTERNAL:
                case UNAVAILABLE:
                default:
                    throw new RuntimeException(
                            "Failed to communicate with user-service: " + e.getStatus().getDescription(),
                            e
                    );
            }
        }
    }

    public UserDTO createUser(RegisterRequestDTO registerRequestDTO) {
        try {
            log.debug("Creating new user: {}", registerRequestDTO.getEmail());

            CreateUserRequest request = CreateUserRequest.newBuilder()
                    .setUsername(registerRequestDTO.getUsername())
                    .setEmail(registerRequestDTO.getEmail())
                    .setPassword(registerRequestDTO.getPassword())
                    .build();

            CreateUserResponse response = userServiceStub.createUser(request);

            log.info("User created successfully: {}", registerRequestDTO.getEmail());
            return UserDTO.fromUserData(response.getUser());

        } catch (StatusRuntimeException e) {
            log.error("gRPC error while creating user: {} - {}",
                    e.getStatus().getCode(),
                    e.getStatus().getDescription());

            switch (e.getStatus().getCode()) {
                case ALREADY_EXISTS:
                    throw new UserAlreadyExistsException(e.getStatus().getDescription());
                case INVALID_ARGUMENT:
                    throw new IllegalArgumentException(e.getStatus().getDescription());
                case INTERNAL:
                case UNAVAILABLE:
                default:
                    throw new RegistrationException(
                            "Failed to communicate with user-service: " + e.getStatus().getDescription(),
                            e
                    );
            }
        }
    }
}