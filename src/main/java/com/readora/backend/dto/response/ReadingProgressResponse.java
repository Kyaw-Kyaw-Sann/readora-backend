package com.readora.backend.dto.response;

import java.time.LocalDateTime;

public record ReadingProgressResponse(BookPublicSummaryResponse book, Integer currentPage, Integer totalPages,
        boolean completed, int progressPercentage, LocalDateTime lastAccessedAt, LocalDateTime updatedAt) {
}
