package com.readora.backend.dto.response;

import java.util.List;

public record PersonalLibrarySummaryResponse(

        PersonalLibraryProfileSummaryResponse profile,

        PersonalLibrarySubscriptionResponse subscription,

        List<BookPublicSummaryResponse> favorites,

        List<ReadingProgressResponse> continueReading,

        List<ListeningProgressResponse> continueListening,

        List<ReadingProgressResponse> recentlyRead,

        List<ListeningProgressResponse> recentlyListened

) {
}