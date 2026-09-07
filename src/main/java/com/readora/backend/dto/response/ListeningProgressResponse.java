package com.readora.backend.dto.response;

import java.time.LocalDateTime;

public record ListeningProgressResponse(BookPublicSummaryResponse book, Integer currentSeconds, Integer durationSeconds,
        boolean completed, int progressPercentage, LocalDateTime lastAccessedAt, LocalDateTime updatedAt) {
}