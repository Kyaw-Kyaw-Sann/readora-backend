package com.readora.backend.dto.response;

import com.readora.backend.enums.BookAccessType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BookPublicDetailResponse(Long id, String title, String description, String isbn, String language,
        LocalDate publicationDate, String author, String coverUrl, Integer pageCount, Integer audioDurationSeconds,
        BookAccessType accessType, Long viewCount, boolean hasPdf, boolean hasAudio, List<CategoryResponse> categories,
        LocalDateTime createdAt) {
}