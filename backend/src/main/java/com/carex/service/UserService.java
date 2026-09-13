package com.carex.service;

import com.carex.entity.User;
import com.carex.entity.enums.Role;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(String name, String email, String passwordHash, Role role, String phone);
    User getUserById(Long id);
    Optional<User> findByEmail(String email);
    List<User> getAllUsers();
    List<User> getUsersByRole(Role role);
    User updateUser(Long id, String name, String phone);
    User setActiveStatus(Long id, boolean active);
    void deleteUser(Long id);
}
