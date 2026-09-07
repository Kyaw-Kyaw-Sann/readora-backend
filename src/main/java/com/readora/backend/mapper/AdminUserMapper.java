package com.readora.backend.mapper;

import com.readora.backend.dto.response.AdminUserDetailResponse;
import com.readora.backend.dto.response.AdminUserSubscriptionResponse;
import com.readora.backend.dto.response.AdminUserSummaryResponse;
import com.readora.backend.entity.Category;
import com.readora.backend.entity.Subscription;
import com.readora.backend.entity.User;

import java.util.List;

public final class AdminUserMapper {

    private AdminUserMapper() {
    }

    public static AdminUserSummaryResponse toSummaryResponse(User user, boolean premiumActive,
            Subscription subscription) {

        return new AdminUserSummaryResponse(user.getId(), user.getName(), user.getEmail(), user.getProfileImageUrl(),
                user.getProvider(), user.isEmailVerified(), user.getRole(), premiumActive,
                subscription == null ? null : subscription.getPlan(),
                subscription == null ? null : subscription.getStatus(), user.getCreatedAt());
    }

    public static AdminUserDetailResponse toDetailResponse(User user, boolean premiumActive,
            Subscription subscription) {

        List<String> interests = user.getInterests().stream().filter(Category::isActive).map(Category::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER).toList();

        return new AdminUserDetailResponse(user.getId(), user.getName(), user.getEmail(), user.getProfileImageUrl(),
                user.getProvider(), user.isEmailVerified(), user.getRole(), interests, premiumActive,
                toSubscriptionResponse(subscription), user.getCreatedAt(), user.getUpdatedAt());
    }

    private static AdminUserSubscriptionResponse toSubscriptionResponse(Subscription subscription) {

        if (subscription == null) {
            return null;
        }

        return new AdminUserSubscriptionResponse(subscription.getId(), subscription.getPlan(), subscription.getStatus(),
                subscription.getStartedAt(), subscription.getExpiresAt(), subscription.getCancelledAt());
    }
}
