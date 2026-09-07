package com.readora.backend.dto.response;

import com.readora.backend.enums.AuthProvider;
import com.readora.backend.enums.Role;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        String profileImageUrl,
        AuthProvider provider,
        boolean emailVerified,
        Role role,
        LocalDateTime createdAt,
        String subscriptionStatus) {
}