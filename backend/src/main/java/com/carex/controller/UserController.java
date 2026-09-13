package com.carex.controller;

import com.carex.dto.user.UserResponse;
import com.carex.dto.user.UserUpdateRequest;
import com.carex.entity.User;
import com.carex.entity.enums.Role;
import com.carex.mapper.UserMapper;
import com.carex.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User profile and account management endpoints")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a list of all registered users (safe profile only)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(userMapper.toResponseList(users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves user profile details by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userMapper.toResponse(user));
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get users by role", description = "Filters users by system role (e.g. PATIENT, DOCTOR, ADMIN, STAFF)")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable Role role) {
        List<User> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(userMapper.toResponseList(users));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile", description = "Updates editable profile fields (name, phone) of a user")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        User updated = userService.updateUser(id, request.getName(), request.getPhone());
        return ResponseEntity.ok(userMapper.toResponse(updated));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Toggle user active status", description = "Activates or deactivates a user account")
    public ResponseEntity<UserResponse> setUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        User updated = userService.setActiveStatus(id, active);
        return ResponseEntity.ok(userMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deactivates/deletes a user from the system")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
