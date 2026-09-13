package com.carex.dto.auth;

import com.carex.entity.enums.Role;

public class LoginResponse {

    private String token;
    private String tokenType;
    private Long userId;
    private String name;
    private String email;
    private Role role;

    public LoginResponse() {
        this.tokenType = "Bearer";
    }

    public LoginResponse(String token, String tokenType, Long userId, String name, String email, Role role) {
        this.token = token;
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static LoginResponse of(String token, Long userId, String name, String email, Role role) {
        return new LoginResponse(token, "Bearer", userId, name, email, role);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
