package com.pm.userservice.service;

import com.pm.userservice.dto.UserRequestDTO;
import com.pm.userservice.dto.UserResponseDTO;
import com.pm.userservice.exception.ForbiddenException;
import com.pm.userservice.model.User;
import com.pm.userservice.model.enums.UserRole;
import com.pm.userservice.repository.UserRepository;
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
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO, String requesterRole) {
        requireAdmin(requesterRole);

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

    public List<UserResponseDTO> getAllUsers(String requesterRole) {
        requireAdmin(requesterRole);

        return userRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(UUID id, UUID requesterUserId, String requesterRole) {
        validateUserScope(id, requesterUserId, requesterRole);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return toResponseDTO(user);
    }

    public UserResponseDTO getUserByUsername(String username, UUID requesterUserId, String requesterRole) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        validateUserScope(user.getId(), requesterUserId, requesterRole);
        return toResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO updateUser(UUID id, UserRequestDTO userRequestDTO, UUID requesterUserId, String requesterRole) {
        validateUserScope(id, requesterUserId, requesterRole);

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
    public void deleteUser(UUID id, UUID requesterUserId, String requesterRole) {
        validateUserScope(id, requesterUserId, requesterRole);

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

    private void validateUserScope(UUID targetUserId, UUID requesterUserId, String requesterRole) {
        if (isAdmin(requesterRole)) {
            return;
        }

        if (!targetUserId.equals(requesterUserId)) {
            throw new ForbiddenException("You do not have access to this user");
        }
    }

    private void requireAdmin(String requesterRole) {
        if (!isAdmin(requesterRole)) {
            throw new ForbiddenException("Only ADMIN can perform this operation");
        }
    }

    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
