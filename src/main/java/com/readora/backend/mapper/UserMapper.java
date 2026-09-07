package com.readora.backend.mapper;

import com.readora.backend.dto.response.UserResponse;
import com.readora.backend.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProvider(),
                user.isEmailVerified(),
                user.getRole());
    }
}