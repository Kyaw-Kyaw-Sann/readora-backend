package com.readora.backend.mapper;

import com.readora.backend.dto.response.BookDetailResponse;
import com.readora.backend.dto.response.BookPublicDetailResponse;
import com.readora.backend.dto.response.BookPublicSummaryResponse;
import com.readora.backend.dto.response.BookSummaryResponse;
import com.readora.backend.dto.response.CategoryResponse;
import com.readora.backend.entity.Book;

import java.util.List;

public final class BookMapper {

    private BookMapper() {
    }

    public static BookDetailResponse toDetailResponse(Book book) {

        return new BookDetailResponse(book.getId(), book.getTitle(), book.getDescription(), book.getIsbn(),
                book.getLanguage(), book.getPublicationDate(), book.getAuthor(), book.getCoverUrl(), book.getPdfUrl(),
                book.getAudioUrl(), book.getPageCount(), book.getAudioDurationSeconds(), book.getAccessType(),
                book.getStatus(), book.getViewCount(), mapCategories(book), book.getCreatedAt(), book.getUpdatedAt());
    }

    public static BookSummaryResponse toSummaryResponse(Book book) {

        return new BookSummaryResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getCoverUrl(),
                book.getAccessType(), book.getStatus(), book.getViewCount(), mapCategories(book), book.getCreatedAt());
    }

    public static BookPublicSummaryResponse toPublicSummaryResponse(Book book) {

        return new BookPublicSummaryResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getCoverUrl(),
                book.getAccessType(), book.getViewCount(), mapCategories(book), book.getCreatedAt());
    }

    public static BookPublicDetailResponse toPublicDetailResponse(Book book) {

        boolean hasPdf = book.getPdfUrl() != null && !book.getPdfUrl().isBlank();

        boolean hasAudio = book.getAudioUrl() != null && !book.getAudioUrl().isBlank();

        return new BookPublicDetailResponse(book.getId(), book.getTitle(), book.getDescription(), book.getIsbn(),
                book.getLanguage(), book.getPublicationDate(), book.getAuthor(), book.getCoverUrl(),
                book.getPageCount(), book.getAudioDurationSeconds(), book.getAccessType(), book.getViewCount(), hasPdf,
                hasAudio, mapCategories(book), book.getCreatedAt());
    }

    private static List<CategoryResponse> mapCategories(Book book) {

        return book.getCategories().stream().filter(category -> category.isActive())
                .sorted((first, second) -> first.getName().compareToIgnoreCase(second.getName()))
                .map(CategoryMapper::toResponse).toList();
    }
}