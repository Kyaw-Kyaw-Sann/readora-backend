package com.readora.backend.dto.response;

import com.readora.backend.enums.SubscriptionPlan;
import com.readora.backend.enums.SubscriptionStatus;

import java.time.LocalDateTime;

public record AdminUserSubscriptionResponse(Long id, SubscriptionPlan plan, SubscriptionStatus status,
        LocalDateTime startedAt, LocalDateTime expiresAt, LocalDateTime cancelledAt) {
}