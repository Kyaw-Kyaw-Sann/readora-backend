package com.readora.backend.dto.response;

import com.readora.backend.enums.BookAccessType;
import com.readora.backend.enums.BookStatus;

import java.time.LocalDateTime;
import java.util.List;

public record BookSummaryResponse(Long id, String title, String author, String coverUrl, BookAccessType accessType,
        BookStatus status, Long viewCount, List<CategoryResponse> categories, LocalDateTime createdAt) {
}