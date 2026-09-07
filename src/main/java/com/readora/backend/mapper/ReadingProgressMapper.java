package com.readora.backend.mapper;

import com.readora.backend.dto.response.ReadingProgressResponse;
import com.readora.backend.entity.Book;
import com.readora.backend.entity.ReadingProgress;

public final class ReadingProgressMapper {

    private ReadingProgressMapper() {
    }

    public static ReadingProgressResponse toResponse(ReadingProgress progress) {

        return new ReadingProgressResponse(BookMapper.toPublicSummaryResponse(progress.getBook()),
                progress.getCurrentPage(), progress.getTotalPages(), progress.isCompleted(),
                calculatePercentage(progress.getCurrentPage(), progress.getTotalPages()), progress.getLastAccessedAt(),
                progress.getUpdatedAt());
    }

    public static ReadingProgressResponse toDefaultResponse(Book book) {

        return new ReadingProgressResponse(BookMapper.toPublicSummaryResponse(book), 1, book.getPageCount(), false, 0,
                null, null);
    }

    private static int calculatePercentage(Integer currentPage, Integer totalPages) {

        if (currentPage == null || totalPages == null || totalPages <= 0) {

            return 0;
        }

        double percentage = (currentPage * 100.0) / totalPages;

        return (int) Math.min(100, Math.round(percentage));
    }
}