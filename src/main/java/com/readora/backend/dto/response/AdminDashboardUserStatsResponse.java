package com.readora.backend.dto.response;

public record AdminDashboardUserStatsResponse(long total, long verified, long unverified, long activePremium) {
}
