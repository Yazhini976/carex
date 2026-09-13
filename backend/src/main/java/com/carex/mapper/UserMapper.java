package com.carex.mapper;

import com.carex.dto.user.UserResponse;
import com.carex.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                Boolean.TRUE.equals(user.getIsActive()),
                user.getCreatedAt()
        );
    }

    public List<UserResponse> toResponseList(List<User> users) {
        if (users == null) {
            return Collections.emptyList();
        }
        return users.stream().map(this::toResponse).toList();
    }
}
