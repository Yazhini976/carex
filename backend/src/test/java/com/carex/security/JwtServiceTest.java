package com.carex.security;

import com.carex.entity.User;
import com.carex.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "carexSuperSecureTestSecretKeyWithAtLeast256BitsLength1234567890!");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L); // 1 hour
    }

    @Test
    @DisplayName("Generate token and extract claims correctly")
    void testGenerateAndExtractClaims() {
        User user = new User("Alice Walker", "alice@carex.com", "hash", Role.PATIENT);
        user.setId(42L);

        String token = jwtService.generateToken(user);
        assertNotNull(token);

        String email = jwtService.extractEmail(token);
        assertEquals("alice@carex.com", email);

        Long userId = jwtService.extractUserId(token);
        assertEquals(42L, userId);

        String role = jwtService.extractRole(token);
        assertEquals("PATIENT", role);
    }

    @Test
    @DisplayName("Token validation against matching principal")
    void testIsTokenValid() {
        User user = new User("Bob Builder", "bob@carex.com", "hash", Role.DOCTOR);
        user.setId(7L);

        String token = jwtService.generateToken(user);
        CustomUserPrincipal principal = new CustomUserPrincipal(user);

        assertTrue(jwtService.isTokenValid(token, principal));
    }

    @Test
    @DisplayName("Expired token is recognized as expired")
    void testExpiredToken() {
        // Set short expiration
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", -1000L);

        User user = new User("Old User", "old@carex.com", "hash", Role.PATIENT);
        user.setId(99L);

        String token = jwtService.generateToken(user);
        assertTrue(jwtService.isTokenExpired(token));
    }
}
