package com.readora.backend.dto.response;

public record AdminDashboardSubscriptionStatsResponse(long total, long active, long expired, long cancelled,
        long monthly, long yearly) {
}
