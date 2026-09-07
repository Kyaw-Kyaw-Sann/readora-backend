package com.readora.backend.mapper;

import com.readora.backend.dto.response.UserProfileResponse;
import com.readora.backend.entity.User;

public final class UserProfileMapper {

    private UserProfileMapper() {
    }

    public static UserProfileResponse toResponse(
            User user,
            String subscriptionStatus) {

        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getProvider(),
                user.isEmailVerified(),
                user.getRole(),
                user.getCreatedAt(),
                subscriptionStatus);
    }
}
