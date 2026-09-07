package com.readora.backend.dto.response;

import com.readora.backend.enums.AuthProvider;
import com.readora.backend.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

public record AdminUserDetailResponse(Long id, String name, String email, String profileImageUrl, AuthProvider provider,
        boolean emailVerified, Role role, List<String> interests, boolean premiumActive,
        AdminUserSubscriptionResponse subscription, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
