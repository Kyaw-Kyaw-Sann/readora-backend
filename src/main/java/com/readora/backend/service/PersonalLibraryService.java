package com.readora.backend.service;

import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.response.BookPublicSummaryResponse;
import com.readora.backend.dto.response.ListeningProgressResponse;
import com.readora.backend.dto.response.PersonalLibraryProfileSummaryResponse;
import com.readora.backend.dto.response.PersonalLibrarySubscriptionResponse;
import com.readora.backend.dto.response.PersonalLibrarySummaryResponse;
import com.readora.backend.dto.response.ReadingProgressResponse;
import com.readora.backend.entity.Category;
import com.readora.backend.entity.Subscription;
import com.readora.backend.entity.User;
import com.readora.backend.exception.ResourceNotFoundException;
import com.readora.backend.repository.SubscriptionRepository;
import com.readora.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PersonalLibraryService {

    private static final int PREVIEW_PAGE = 0;
    private static final int PREVIEW_SIZE = 5;

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    private final SubscriptionService subscriptionService;
    private final FavoriteService favoriteService;
    private final ReadingProgressService readingProgressService;
    private final ListeningProgressService listeningProgressService;

    @Transactional
    public PersonalLibrarySummaryResponse getLibrarySummary(String email) {

        User user = getUserByEmail(email);

        PersonalLibraryProfileSummaryResponse profile = buildProfileSummary(user);

        PersonalLibrarySubscriptionResponse subscription = buildSubscriptionSummary(user);

        PageResponse<BookPublicSummaryResponse> favoritesPage = favoriteService.getFavorites(email, PREVIEW_PAGE,
                PREVIEW_SIZE);

        PageResponse<ReadingProgressResponse> continueReadingPage = readingProgressService.getContinueReading(email,
                PREVIEW_PAGE, PREVIEW_SIZE);

        PageResponse<ListeningProgressResponse> continueListeningPage = listeningProgressService
                .getContinueListening(email, PREVIEW_PAGE, PREVIEW_SIZE);

        PageResponse<ReadingProgressResponse> recentlyReadPage = readingProgressService.getRecentlyRead(email,
                PREVIEW_PAGE, PREVIEW_SIZE);

        PageResponse<ListeningProgressResponse> recentlyListenedPage = listeningProgressService
                .getRecentlyListened(email, PREVIEW_PAGE, PREVIEW_SIZE);

        return new PersonalLibrarySummaryResponse(profile, subscription, favoritesPage.getContent(),
                continueReadingPage.getContent(), continueListeningPage.getContent(), recentlyReadPage.getContent(),
                recentlyListenedPage.getContent());
    }

    private PersonalLibraryProfileSummaryResponse buildProfileSummary(User user) {

        List<String> interests = user.getInterests().stream().filter(Category::isActive).map(Category::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER).toList();

        return new PersonalLibraryProfileSummaryResponse(user.getId(), user.getName(), user.getEmail(),
                user.getProfileImageUrl(), user.getProvider(), user.isEmailVerified(), user.getRole(), interests,
                user.getCreatedAt());
    }

    private PersonalLibrarySubscriptionResponse buildSubscriptionSummary(User user) {

        boolean premiumActive = subscriptionService.hasActiveSubscription(user.getId());

        Optional<Subscription> latestSubscription = subscriptionRepository
                .findTopByUser_IdOrderByStartedAtDesc(user.getId());

        if (latestSubscription.isEmpty()) {
            return new PersonalLibrarySubscriptionResponse(false, null, null, null, null, null);
        }

        Subscription subscription = latestSubscription.get();

        return new PersonalLibrarySubscriptionResponse(premiumActive, subscription.getPlan(), subscription.getStatus(),
                subscription.getStartedAt(), subscription.getExpiresAt(), subscription.getCancelledAt());
    }

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
