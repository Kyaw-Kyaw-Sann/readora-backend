package com.readora.backend.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(Long id, Long bookId, ReviewUserResponse user, Integer rating, String comment,
        LocalDateTime createdAt, LocalDateTime updatedAt) {
}