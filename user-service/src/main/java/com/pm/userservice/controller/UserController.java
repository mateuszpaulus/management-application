package com.pm.userservice.controller;

import com.pm.userservice.dto.UserRequestDTO;
import com.pm.userservice.dto.UserResponseDTO;
import com.pm.userservice.security.AuthContext;
import com.pm.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "users", description = "API for managing Users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create a new User")
    public ResponseEntity<UserResponseDTO> createUser(
            AuthContext authContext,
            @Valid @RequestBody UserRequestDTO userRequestDTO
    ) {
        UserResponseDTO createdUser = userService.createUser(userRequestDTO, authContext);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get Users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(AuthContext authContext) {
        List<UserResponseDTO> users = userService.getAllUsers(authContext);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get User by Id")
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable UUID id,
            AuthContext authContext
    ) {
        UserResponseDTO user = userService.getUserById(id, authContext);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get User by Username")
    public ResponseEntity<UserResponseDTO> getUserByUsername(
            @PathVariable String username,
            AuthContext authContext
    ) {
        UserResponseDTO user = userService.getUserByUsername(username, authContext);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a User")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable UUID id,
            AuthContext authContext,
            @Valid @RequestBody UserRequestDTO userRequestDTO
    ) {
        UserResponseDTO updatedUser = userService.updateUser(id, userRequestDTO, authContext);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a User")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id,
            AuthContext authContext
    ) {
        userService.deleteUser(id, authContext);
        return ResponseEntity.noContent().build();
    }
}
