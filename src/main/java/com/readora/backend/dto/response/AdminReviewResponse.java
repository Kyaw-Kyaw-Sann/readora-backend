package com.readora.backend.dto.response;

import java.time.LocalDateTime;

public record AdminReviewResponse(Long id, AdminReviewUserResponse user, AdminReviewBookResponse book, Integer rating,
        String comment, LocalDateTime createdAt, LocalDateTime updatedAt) {
}