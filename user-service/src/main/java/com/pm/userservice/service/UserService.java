package com.pm.userservice.service;

import com.pm.userservice.dto.UserRequestDTO;
import com.pm.userservice.dto.UserResponseDTO;
import com.pm.userservice.exception.ForbiddenException;
import com.pm.userservice.model.User;
import com.pm.userservice.model.enums.UserRole;
import com.pm.userservice.repository.UserRepository;
import com.pm.userservice.security.AuthContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO, AuthContext authContext) {
        requireAdmin(authContext);

        if (userRepository.existsByUsername(userRequestDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(userRequestDTO.getUsername());
        user.setEmail(userRequestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        user.setRole(UserRole.USER);

        User savedUser = userRepository.save(user);
        return toResponseDTO(savedUser);
    }

    public List<UserResponseDTO> getAllUsers(AuthContext authContext) {
        requireAdmin(authContext);

        return userRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(UUID id, AuthContext authContext) {
        validateUserScope(id, authContext);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return toResponseDTO(user);
    }

    public UserResponseDTO getUserByUsername(String username, AuthContext authContext) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        validateUserScope(user.getId(), authContext);
        return toResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO updateUser(UUID id, UserRequestDTO userRequestDTO, AuthContext authContext) {
        validateUserScope(id, authContext);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (!user.getUsername().equals(userRequestDTO.getUsername()) &&
                userRepository.existsByUsername(userRequestDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (!user.getEmail().equals(userRequestDTO.getEmail()) &&
                userRepository.existsByEmail(userRequestDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        user.setUsername(userRequestDTO.getUsername());
        user.setEmail(userRequestDTO.getEmail());
        if (userRequestDTO.getPassword() != null && !userRequestDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return toResponseDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(UUID id, AuthContext authContext) {
        validateUserScope(id, authContext);

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    private void validateUserScope(UUID targetUserId, AuthContext authContext) {
        if (authContext.isAdmin()) {
            return;
        }

        if (!targetUserId.equals(authContext.userId())) {
            throw new ForbiddenException("You do not have access to this user");
        }
    }

    private void requireAdmin(AuthContext authContext) {
        if (!authContext.isAdmin()) {
            throw new ForbiddenException("Only ADMIN can perform this operation");
        }
    }
}
