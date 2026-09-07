package com.readora.backend.dto.response;

import com.readora.backend.enums.SubscriptionPlan;
import com.readora.backend.enums.SubscriptionStatus;

import java.time.LocalDateTime;

public record AdminSubscriptionResponse(Long id, AdminSubscriptionUserResponse user, SubscriptionPlan plan,
        SubscriptionStatus status, boolean premiumActive, LocalDateTime startedAt, LocalDateTime expiresAt,
        LocalDateTime cancelledAt) {
}
