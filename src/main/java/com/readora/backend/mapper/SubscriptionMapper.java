package com.readora.backend.mapper;

import com.readora.backend.dto.response.SubscriptionResponse;
import com.readora.backend.entity.Subscription;

public final class SubscriptionMapper {

    private SubscriptionMapper() {
    }

    public static SubscriptionResponse toResponse(Subscription subscription) {
        return new SubscriptionResponse(subscription.getId(), subscription.getPlan(), subscription.getStatus(),
                subscription.getStartedAt(), subscription.getExpiresAt(), subscription.getCancelledAt());
    }
}