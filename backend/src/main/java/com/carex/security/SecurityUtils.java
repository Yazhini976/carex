package com.carex.security;

import com.carex.entity.enums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Optional<CustomUserPrincipal> getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        if (auth.getPrincipal() instanceof CustomUserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static Optional<Long> getCurrentUserId() {
        return getCurrentPrincipal().map(CustomUserPrincipal::getId);
    }

    public static Optional<String> getCurrentUserEmail() {
        return getCurrentPrincipal().map(CustomUserPrincipal::getEmail);
    }

    public static Optional<Role> getCurrentUserRole() {
        return getCurrentPrincipal().map(CustomUserPrincipal::getRole);
    }

    public static boolean isAdmin() {
        return getCurrentPrincipal()
                .map(p -> p.getRole() == Role.ADMIN)
                .orElse(false);
    }

    public static boolean isCurrentUser(Long targetUserId) {
        if (targetUserId == null) return false;
        return getCurrentPrincipal()
                .map(p -> p.getId().equals(targetUserId) || p.getRole() == Role.ADMIN)
                .orElse(false);
    }
}
