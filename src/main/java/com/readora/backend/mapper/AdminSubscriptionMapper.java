package com.readora.backend.mapper;

import com.readora.backend.dto.response.AdminSubscriptionResponse;
import com.readora.backend.dto.response.AdminSubscriptionUserResponse;
import com.readora.backend.entity.Subscription;
import com.readora.backend.entity.User;
import com.readora.backend.enums.SubscriptionStatus;

import java.time.LocalDateTime;

public final class AdminSubscriptionMapper {

    private AdminSubscriptionMapper() {
    }

    public static AdminSubscriptionResponse toResponse(Subscription subscription, LocalDateTime now) {

        SubscriptionStatus effectiveStatus = getEffectiveStatus(subscription, now);

        boolean premiumActive = effectiveStatus == SubscriptionStatus.ACTIVE;

        return new AdminSubscriptionResponse(subscription.getId(), toUserResponse(subscription.getUser()),
                subscription.getPlan(), effectiveStatus, premiumActive, subscription.getStartedAt(),
                subscription.getExpiresAt(), subscription.getCancelledAt());
    }

    private static AdminSubscriptionUserResponse toUserResponse(User user) {

        return new AdminSubscriptionUserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getProfileImageUrl());
    }

    private static SubscriptionStatus getEffectiveStatus(Subscription subscription, LocalDateTime now) {

        if (subscription.getStatus() == SubscriptionStatus.ACTIVE && subscription.getExpiresAt() != null
                && !subscription.getExpiresAt().isAfter(now)) {

            return SubscriptionStatus.EXPIRED;
        }

        return subscription.getStatus();
    }
}
