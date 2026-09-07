package com.readora.backend.dto.response;

import com.readora.backend.enums.AuthProvider;
import com.readora.backend.enums.Role;
import com.readora.backend.enums.SubscriptionPlan;
import com.readora.backend.enums.SubscriptionStatus;

import java.time.LocalDateTime;

public record AdminUserSummaryResponse(Long id, String name, String email, String profileImageUrl,
        AuthProvider provider, boolean emailVerified, Role role, boolean premiumActive,
        SubscriptionPlan subscriptionPlan, SubscriptionStatus subscriptionStatus, LocalDateTime createdAt) {
}