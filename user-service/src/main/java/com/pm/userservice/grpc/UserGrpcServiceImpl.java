package com.pm.userservice.grpc;

import com.pm.grpc.user.GetUserByIdRequest;
import com.pm.grpc.user.UserData;
import com.pm.grpc.user.UserResponse;
import com.pm.grpc.user.UserServiceGrpc.UserServiceImplBase;
import com.pm.grpc.user.ValidateCredentialsRequest;
import com.pm.grpc.user.ValidateCredentialsResponse;
import com.pm.userservice.model.User;
import com.pm.userservice.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Optional;
import java.util.UUID;

@GrpcService
public class UserGrpcServiceImpl extends UserServiceImplBase {

    private final UserRepository userRepository;

    public UserGrpcServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                UserResponse response = UserResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("User found")
                        .setUser(UserData.newBuilder()
                                .setId(user.getId().toString())
                                .setUsername(user.getUsername())
                                .setEmail(user.getEmail())
                                .setRole(user.getRole() != null ? user.getRole().toString() : "USER")
                                .build())
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                UserResponse response = UserResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("User not found")
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (Exception e) {
            responseObserver.onError(new RuntimeException("Error fetching user: " + e.getMessage()));
        }
    }

    @Override
    public void validateCredentials(
            ValidateCredentialsRequest request,
            StreamObserver<com.pm.grpc.user.ValidateCredentialsResponse> responseObserver) {

        String email = request.getEmail();
        String password = request.getPassword();

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // TODO: Replace with BCrypt password verification
            if (password.equals(user.getPassword())) {
                ValidateCredentialsResponse response = ValidateCredentialsResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Authentication successful")
                        .setUser(UserData.newBuilder()
                                .setId(user.getId().toString())
                                .setUsername(user.getUsername())
                                .setEmail(user.getEmail())
                                .setRole(user.getRole() != null ? user.getRole().toString() : "USER")
                                .build())
                        .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }
        }

        ValidateCredentialsResponse response = ValidateCredentialsResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Invalid credentials")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}