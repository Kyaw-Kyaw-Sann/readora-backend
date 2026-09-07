package com.readora.backend.service;

import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.response.RecommendationResponse;
import com.readora.backend.entity.Book;
import com.readora.backend.entity.Category;
import com.readora.backend.entity.Favorite;
import com.readora.backend.entity.ReadingProgress;
import com.readora.backend.entity.User;
import com.readora.backend.enums.BookStatus;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.exception.ResourceNotFoundException;
import com.readora.backend.mapper.BookMapper;
import com.readora.backend.repository.BookRepository;
import com.readora.backend.repository.FavoriteRepository;
import com.readora.backend.repository.ReadingProgressRepository;
import com.readora.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final int INTEREST_MATCH_SCORE = 5;
    private static final int READING_MATCH_SCORE = 3;
    private static final int FAVORITE_MATCH_SCORE = 2;
    private static final int POPULAR_SCORE = 1;
    private static final int NEW_SCORE = 1;

    private static final int NEW_BOOK_DAYS = 30;

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final FavoriteRepository favoriteRepository;

    @Transactional(readOnly = true)
    public PageResponse<RecommendationResponse> getRecommendations(String email, int page, int size) {

        validatePagination(page, size);

        User user = getUserByEmail(email);

        Set<Long> interestCategoryIds = getInterestCategoryIds(user);

        Set<Long> readingCategoryIds = getReadingCategoryIds(user.getId());

        Set<Long> favoriteCategoryIds = getFavoriteCategoryIds(user.getId());

        Set<Long> popularBookIds = getPopularBookIds();

        LocalDateTime newBookThreshold = LocalDateTime.now().minusDays(NEW_BOOK_DAYS);

        List<Book> publishedBooks = bookRepository.findAllByStatus(BookStatus.PUBLISHED);

        List<ScoredBook> scoredBooks = publishedBooks.stream()
                .map(book -> scoreBook(book, interestCategoryIds, readingCategoryIds, favoriteCategoryIds,
                        popularBookIds, newBookThreshold))
                .filter(scoredBook -> scoredBook.score() > 0)
                .sorted(Comparator.comparingInt(ScoredBook::score).reversed()
                        .thenComparing(scoredBook -> safeViewCount(scoredBook.book()), Comparator.reverseOrder())
                        .thenComparing(scoredBook -> scoredBook.book().getCreatedAt(),
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        int start = Math.min(page * size, scoredBooks.size());

        int end = Math.min(start + size, scoredBooks.size());

        List<RecommendationResponse> content = scoredBooks.subList(start, end).stream()
                .map(scoredBook -> new RecommendationResponse(BookMapper.toPublicSummaryResponse(scoredBook.book()),
                        scoredBook.score()))
                .toList();

        Page<RecommendationResponse> responsePage = new PageImpl<>(content, PageRequest.of(page, size),
                scoredBooks.size());

        return PageResponse.from(responsePage);
    }

    private ScoredBook scoreBook(Book book, Set<Long> interestCategoryIds, Set<Long> readingCategoryIds,
            Set<Long> favoriteCategoryIds, Set<Long> popularBookIds, LocalDateTime newBookThreshold) {

        int score = 0;

        Set<Long> bookCategoryIds = book.getCategories().stream().filter(Category::isActive).map(Category::getId)
                .collect(java.util.stream.Collectors.toSet());

        if (hasCategoryMatch(bookCategoryIds, interestCategoryIds)) {

            score += INTEREST_MATCH_SCORE;
        }

        if (hasCategoryMatch(bookCategoryIds, readingCategoryIds)) {

            score += READING_MATCH_SCORE;
        }

        if (hasCategoryMatch(bookCategoryIds, favoriteCategoryIds)) {

            score += FAVORITE_MATCH_SCORE;
        }

        if (popularBookIds.contains(book.getId())) {
            score += POPULAR_SCORE;
        }

        if (isNewBook(book, newBookThreshold)) {

            score += NEW_SCORE;
        }

        return new ScoredBook(book, score);
    }

    private Set<Long> getInterestCategoryIds(User user) {

        Set<Long> categoryIds = new HashSet<>();

        for (Category category : user.getInterests()) {

            if (category.isActive()) {
                categoryIds.add(category.getId());
            }
        }

        return categoryIds;
    }

    private Set<Long> getReadingCategoryIds(Long userId) {

        List<ReadingProgress> progressList = readingProgressRepository.findAllByUser_Id(userId);

        Set<Long> categoryIds = new HashSet<>();

        for (ReadingProgress progress : progressList) {

            Book book = progress.getBook();

            for (Category category : book.getCategories()) {

                if (category.isActive()) {
                    categoryIds.add(category.getId());
                }
            }
        }

        return categoryIds;
    }

    private Set<Long> getFavoriteCategoryIds(Long userId) {

        List<Favorite> favorites = favoriteRepository.findAllByUser_Id(userId);

        Set<Long> categoryIds = new HashSet<>();

        for (Favorite favorite : favorites) {

            Book book = favorite.getBook();

            for (Category category : book.getCategories()) {

                if (category.isActive()) {
                    categoryIds.add(category.getId());
                }
            }
        }

        return categoryIds;
    }

    private Set<Long> getPopularBookIds() {

        List<Book> popularBooks = bookRepository
                .findTop10ByStatusOrderByViewCountDescCreatedAtDesc(BookStatus.PUBLISHED);

        Set<Long> bookIds = new HashSet<>();

        for (Book book : popularBooks) {
            bookIds.add(book.getId());
        }

        return bookIds;
    }

    private boolean hasCategoryMatch(Set<Long> bookCategoryIds, Set<Long> signalCategoryIds) {

        if (bookCategoryIds.isEmpty() || signalCategoryIds.isEmpty()) {

            return false;
        }

        for (Long categoryId : bookCategoryIds) {

            if (signalCategoryIds.contains(categoryId)) {

                return true;
            }
        }

        return false;
    }

    private boolean isNewBook(Book book, LocalDateTime threshold) {

        return book.getCreatedAt() != null && !book.getCreatedAt().isBefore(threshold);
    }

    private Long safeViewCount(Book book) {

        return book.getViewCount() == null ? 0L : book.getViewCount();
    }

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validatePagination(int page, int size) {

        if (page < 0) {
            throw new BadRequestException("Page number must be 0 or greater");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }
    }

    private record ScoredBook(Book book, int score) {
    }
}