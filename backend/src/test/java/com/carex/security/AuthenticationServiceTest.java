package com.carex.security;

import com.carex.dto.auth.LoginRequest;
import com.carex.dto.auth.LoginResponse;
import com.carex.dto.auth.RegisterRequest;
import com.carex.dto.user.UserResponse;
import com.carex.entity.User;
import com.carex.entity.enums.Role;
import com.carex.exception.BusinessRuleException;
import com.carex.exception.DuplicateResourceException;
import com.carex.mapper.UserMapper;
import com.carex.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.carex.repository.PatientRepository patientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private UserMapper userMapper;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        authenticationService = new AuthenticationService(userRepository, patientRepository, passwordEncoder, jwtService, userMapper);
    }

    @Test
    @DisplayName("Register new user successfully")
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest("David Clark", "david@carex.com", "SecretPass123", "+1-555-1234", Role.PATIENT);

        when(userRepository.existsByEmail("david@carex.com")).thenReturn(false);
        when(passwordEncoder.encode("SecretPass123")).thenReturn("hashedPassword123");

        User savedUser = new User("David Clark", "david@carex.com", "hashedPassword123", Role.PATIENT);
        savedUser.setId(15L);
        savedUser.setPhone("+1-555-1234");
        savedUser.setIsActive(Boolean.TRUE);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authenticationService.register(request);

        assertNotNull(response);
        assertEquals(15L, response.getId());
        assertEquals("david@carex.com", response.getEmail());
        assertEquals(Role.PATIENT, response.getRole());
    }

    @Test
    @DisplayName("Register with existing email throws DuplicateResourceException")
    void testRegister_DuplicateEmail() {
        RegisterRequest request = new RegisterRequest("David Clark", "david@carex.com", "SecretPass123", null, Role.PATIENT);
        when(userRepository.existsByEmail("david@carex.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authenticationService.register(request));
    }

    @Test
    @DisplayName("Login with valid credentials returns JWT token")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("david@carex.com", "SecretPass123");

        User user = new User("David Clark", "david@carex.com", "hashedPassword123", Role.PATIENT);
        user.setId(15L);
        user.setIsActive(Boolean.TRUE);

        when(userRepository.findByEmail("david@carex.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SecretPass123", "hashedPassword123")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("mocked.jwt.token");

        LoginResponse response = authenticationService.login(request);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(15L, response.getUserId());
        assertEquals("PATIENT", response.getRole().name());
    }

    @Test
    @DisplayName("Login with invalid password throws BadCredentialsException")
    void testLogin_InvalidPassword() {
        LoginRequest request = new LoginRequest("david@carex.com", "WrongPassword");

        User user = new User("David Clark", "david@carex.com", "hashedPassword123", Role.PATIENT);
        user.setId(15L);
        user.setIsActive(Boolean.TRUE);

        when(userRepository.findByEmail("david@carex.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "hashedPassword123")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authenticationService.login(request));
    }

    @Test
    @DisplayName("Login with deactivated user account throws BusinessRuleException")
    void testLogin_DeactivatedAccount() {
        LoginRequest request = new LoginRequest("david@carex.com", "SecretPass123");

        User user = new User("David Clark", "david@carex.com", "hashedPassword123", Role.PATIENT);
        user.setId(15L);
        user.setIsActive(Boolean.FALSE);

        when(userRepository.findByEmail("david@carex.com")).thenReturn(Optional.of(user));

        assertThrows(BusinessRuleException.class, () -> authenticationService.login(request));
    }
}
