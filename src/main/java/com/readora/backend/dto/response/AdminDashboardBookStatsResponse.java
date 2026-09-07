package com.readora.backend.dto.response;

public record AdminDashboardBookStatsResponse(long total, long published, long draft, long archived, long free,
        long premium) {
}