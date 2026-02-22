package com.pm.userservice.grpc;

import com.pm.grpc.user.*;
import com.pm.grpc.user.UserServiceGrpc.UserServiceImplBase;
import com.pm.userservice.model.User;
import com.pm.userservice.model.enums.UserRole;
import com.pm.userservice.repository.UserRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@GrpcService
public class UserGrpcServiceImpl extends UserServiceImplBase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserGrpcServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

    }

    @Override
    public void validateCredentials(
            ValidateCredentialsRequest request,
            StreamObserver<ValidateCredentialsResponse> responseObserver) {

        try {
            String email = request.getEmail();
            String password = request.getPassword();

            if (email.trim().isEmpty()) {
                responseObserver.onError(
                        Status.INVALID_ARGUMENT
                                .withDescription("Email is required")
                                .asRuntimeException()
                );
                return;
            }

            if (password.trim().isEmpty()) {
                responseObserver.onError(
                        Status.INVALID_ARGUMENT
                                .withDescription("Password is required")
                                .asRuntimeException()
                );
                return;
            }

            Optional<User> userOpt = userRepository.findByEmail(email);

            if (userOpt.isEmpty()) {
                responseObserver.onError(
                        Status.UNAUTHENTICATED
                                .withDescription("Invalid email or password")
                                .asRuntimeException()
                );
                return;
            }

            User user = userOpt.get();

            if (!passwordEncoder.matches(password, user.getPassword())) {
                responseObserver.onError(
                        Status.UNAUTHENTICATED
                                .withDescription("Invalid email or password")
                                .asRuntimeException()
                );
                return;
            }

            ValidateCredentialsResponse response = ValidateCredentialsResponse.newBuilder()
                    .setUser(UserData.newBuilder()
                            .setId(user.getId().toString())
                            .setUsername(user.getUsername())
                            .setEmail(user.getEmail())
                            .setRole(user.getRole().toString())
                            .build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Authentication service error")
                            .asRuntimeException()
            );
        }
    }

    @Override
    public void createUser(
            CreateUserRequest request,
            StreamObserver<CreateUserResponse> responseObserver) {

        try {
            String username = request.getUsername();
            String email = request.getEmail();
            String password = request.getPassword();

            if (userRepository.existsByEmail(email)) {
                responseObserver.onError(
                        Status.ALREADY_EXISTS
                                .withDescription("User with email '" + email + "' already exists")
                                .asRuntimeException()
                );
                return;
            }

            if (userRepository.existsByUsername(username)) {
                responseObserver.onError(
                        Status.ALREADY_EXISTS
                                .withDescription("User with username '" + username + "' already exists")
                                .asRuntimeException()
                );
                return;
            }

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode(password));
            newUser.setRole(UserRole.USER);

            User savedUser = userRepository.save(newUser);

            CreateUserResponse response = CreateUserResponse.newBuilder()
                    .setUser(UserData.newBuilder()
                            .setId(savedUser.getId().toString())
                            .setUsername(savedUser.getUsername())
                            .setEmail(savedUser.getEmail())
                            .setRole(savedUser.getRole().toString())
                            .build())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Failed to create user: " + e.getMessage())
                            .asRuntimeException()
            );
        }
    }

}