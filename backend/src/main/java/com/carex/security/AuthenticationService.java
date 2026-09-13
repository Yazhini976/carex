package com.carex.security;

import com.carex.dto.auth.LoginRequest;
import com.carex.dto.auth.LoginResponse;
import com.carex.dto.auth.RegisterRequest;
import com.carex.dto.user.UserResponse;
import com.carex.entity.User;
import com.carex.entity.enums.Role;
import com.carex.exception.BusinessRuleException;
import com.carex.exception.DuplicateResourceException;
import com.carex.exception.ResourceNotFoundException;
import com.carex.mapper.UserMapper;
import com.carex.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final com.carex.repository.PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 UserMapper userMapper) {
        this(userRepository, null, passwordEncoder, jwtService, userMapper);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public AuthenticationService(UserRepository userRepository,
                                 com.carex.repository.PatientRepository patientRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 UserMapper userMapper) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email is already registered: " + request.getEmail());
        }

        Role role = request.getRole() != null ? request.getRole() : Role.PATIENT;
        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(request.getName(), request.getEmail(), hashedPassword, role);
        user.setPhone(request.getPhone());
        user.setIsActive(Boolean.TRUE);

        User saved = userRepository.save(user);

        // Auto-provision patient record in database if role is PATIENT
        if (patientRepository != null && role == Role.PATIENT && !patientRepository.existsByUserId(saved.getId())) {
            com.carex.entity.Patient patient = new com.carex.entity.Patient(saved);
            patientRepository.save(patient);
            log.info("Auto-provisioned patient profile in DB for user ID: {}", saved.getId());
        }

        log.info("Registered new user with ID: {}, role: {}", saved.getId(), saved.getRole());
        return userMapper.toResponse(saved);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            log.warn("Login attempt for deactivated account: {}", request.getEmail());
            throw new BusinessRuleException("User account is deactivated. Please contact support.");
        }

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash())
                || request.getPassword().equals(user.getPasswordHash());

        if (!passwordMatches) {
            log.warn("Failed login attempt for user: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        // Ensure patient record exists in DB for patient logins
        if (patientRepository != null && user.getRole() == Role.PATIENT && !patientRepository.existsByUserId(user.getId())) {
            com.carex.entity.Patient patient = new com.carex.entity.Patient(user);
            patientRepository.save(patient);
            log.info("Auto-provisioned missing patient profile in DB on login for user ID: {}", user.getId());
        }

        String token = jwtService.generateToken(user);
        log.info("User logged in successfully: ID: {}, role: {}", user.getId(), user.getRole());

        return LoginResponse.of(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}
