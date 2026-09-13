package com.carex.service.impl;

import com.carex.entity.User;
import com.carex.entity.enums.Role;
import com.carex.exception.DuplicateResourceException;
import com.carex.exception.ResourceNotFoundException;
import com.carex.repository.UserRepository;
import com.carex.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public User createUser(String name, String email, String passwordHash, Role role, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw DuplicateResourceException.of("User", "email", email);
        }
        User user = new User(name, email, passwordHash, role);
        user.setPhone(phone);
        User saved = userRepository.save(user);
        log.info("Created user id={} email={} role={}", saved.getId(), email, role);
        return saved;
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Override
    @Transactional
    public User updateUser(Long id, String name, String phone) {
        User user = getUserById(id);
        if (name != null) user.setName(name);
        if (phone != null) user.setPhone(phone);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User setActiveStatus(Long id, boolean active) {
        User user = getUserById(id);
        user.setIsActive(active);
        log.info("Set user id={} active={}", id, active);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        getUserById(id); // ensure existence
        userRepository.deleteById(id);
        log.info("Deleted user id={}", id);
    }
}
