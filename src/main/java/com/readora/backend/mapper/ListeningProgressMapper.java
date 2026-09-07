package com.readora.backend.mapper;

import com.readora.backend.dto.response.ListeningProgressResponse;
import com.readora.backend.entity.Book;
import com.readora.backend.entity.ListeningProgress;

public final class ListeningProgressMapper {

    private ListeningProgressMapper() {
    }

    public static ListeningProgressResponse toResponse(ListeningProgress progress) {

        return new ListeningProgressResponse(BookMapper.toPublicSummaryResponse(progress.getBook()),
                progress.getCurrentSeconds(), progress.getDurationSeconds(), progress.isCompleted(),
                calculatePercentage(progress.getCurrentSeconds(), progress.getDurationSeconds()),
                progress.getLastAccessedAt(), progress.getUpdatedAt());
    }

    public static ListeningProgressResponse toDefaultResponse(Book book) {

        return new ListeningProgressResponse(BookMapper.toPublicSummaryResponse(book), 0,
                book.getAudioDurationSeconds(), false, 0, null, null);
    }

    private static int calculatePercentage(Integer currentSeconds, Integer durationSeconds) {

        if (currentSeconds == null || durationSeconds == null || durationSeconds <= 0) {

            return 0;
        }

        double percentage = (currentSeconds * 100.0) / durationSeconds;

        return (int) Math.min(100, Math.round(percentage));
    }
}