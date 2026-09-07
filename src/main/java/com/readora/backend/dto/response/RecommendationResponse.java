package com.readora.backend.dto.response;

public record RecommendationResponse(BookPublicSummaryResponse book, int score) {
}