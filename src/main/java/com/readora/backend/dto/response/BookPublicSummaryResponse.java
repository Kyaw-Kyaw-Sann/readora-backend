package com.readora.backend.dto.response;

import com.readora.backend.enums.BookAccessType;

import java.time.LocalDateTime;
import java.util.List;

public record BookPublicSummaryResponse(Long id, String title, String author, String coverUrl,
        BookAccessType accessType, Long viewCount, List<CategoryResponse> categories, LocalDateTime createdAt) {
}