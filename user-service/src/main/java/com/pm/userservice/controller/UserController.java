package com.pm.userservice.controller;

import com.pm.userservice.dto.UserRequestDTO;
import com.pm.userservice.dto.UserResponseDTO;
import com.pm.userservice.exception.UnauthorizedException;
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
    private static final String USER_ID_HEADER = "X-Auth-User-Id";
    private static final String USER_ROLE_HEADER = "X-Auth-User-Role";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create a new User")
    public ResponseEntity<UserResponseDTO> createUser(
            @RequestHeader(value = USER_ROLE_HEADER, required = false) String requesterRole,
            @Valid @RequestBody UserRequestDTO userRequestDTO
    ) {
        UserResponseDTO createdUser = userService.createUser(userRequestDTO, requireRole(requesterRole));
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get Users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(
            @RequestHeader(value = USER_ROLE_HEADER, required = false) String requesterRole
    ) {
        List<UserResponseDTO> users = userService.getAllUsers(requireRole(requesterRole));
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get User by Id")
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable UUID id,
            @RequestHeader(value = USER_ID_HEADER, required = false) String requesterUserId,
            @RequestHeader(value = USER_ROLE_HEADER, required = false) String requesterRole
    ) {
        UserResponseDTO user = userService.getUserById(id, requireUserId(requesterUserId), requireRole(requesterRole));
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get User by Username")
    public ResponseEntity<UserResponseDTO> getUserByUsername(
            @PathVariable String username,
            @RequestHeader(value = USER_ID_HEADER, required = false) String requesterUserId,
            @RequestHeader(value = USER_ROLE_HEADER, required = false) String requesterRole
    ) {
        UserResponseDTO user = userService.getUserByUsername(username, requireUserId(requesterUserId), requireRole(requesterRole));
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a User")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable UUID id,
            @RequestHeader(value = USER_ID_HEADER, required = false) String requesterUserId,
            @RequestHeader(value = USER_ROLE_HEADER, required = false) String requesterRole,
            @Valid @RequestBody UserRequestDTO userRequestDTO) {
        UserResponseDTO updatedUser = userService.updateUser(id, userRequestDTO, requireUserId(requesterUserId), requireRole(requesterRole));
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a User")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id,
            @RequestHeader(value = USER_ID_HEADER, required = false) String requesterUserId,
            @RequestHeader(value = USER_ROLE_HEADER, required = false) String requesterRole
    ) {
        userService.deleteUser(id, requireUserId(requesterUserId), requireRole(requesterRole));
        return ResponseEntity.noContent().build();
    }

    private UUID requireUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new UnauthorizedException("Missing authentication header: " + USER_ID_HEADER);
        }

        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid authentication header: " + USER_ID_HEADER);
        }
    }

    private String requireRole(String roleHeader) {
        if (roleHeader == null || roleHeader.isBlank()) {
            throw new UnauthorizedException("Missing authentication header: " + USER_ROLE_HEADER);
        }
        return roleHeader;
    }
}
