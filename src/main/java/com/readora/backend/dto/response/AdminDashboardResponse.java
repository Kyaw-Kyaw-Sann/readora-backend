package com.readora.backend.dto.response;

public record AdminDashboardResponse(AdminDashboardUserStatsResponse users, AdminDashboardBookStatsResponse books,
        AdminDashboardReviewStatsResponse reviews, AdminDashboardSubscriptionStatsResponse subscriptions) {
}