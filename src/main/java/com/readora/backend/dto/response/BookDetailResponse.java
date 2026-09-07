package com.readora.backend.dto.response;

import com.readora.backend.enums.BookAccessType;
import com.readora.backend.enums.BookStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BookDetailResponse(Long id, String title, String description, String isbn, String language,
        LocalDate publicationDate, String author, String coverUrl, String pdfUrl, String audioUrl, Integer pageCount,
        Integer audioDurationSeconds, BookAccessType accessType, BookStatus status, Long viewCount,
        List<CategoryResponse> categories, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
