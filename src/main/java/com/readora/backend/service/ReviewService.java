package com.readora.backend.service;

import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.request.CreateReviewRequest;
import com.readora.backend.dto.request.UpdateReviewRequest;
import com.readora.backend.dto.response.ReviewResponse;
import com.readora.backend.dto.response.ReviewSummaryResponse;
import com.readora.backend.entity.Book;
import com.readora.backend.entity.Review;
import com.readora.backend.entity.User;
import com.readora.backend.enums.BookStatus;
import com.readora.backend.enums.ReviewSort;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.exception.ResourceNotFoundException;
import com.readora.backend.mapper.ReviewMapper;
import com.readora.backend.repository.BookRepository;
import com.readora.backend.repository.ReviewRepository;
import com.readora.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Transactional
    public ReviewResponse createReview(String email, Long bookId, CreateReviewRequest request) {

        User user = getUserByEmail(email);
        Book book = getPublishedBook(bookId);

        boolean alreadyReviewed = reviewRepository.existsByUser_IdAndBook_Id(user.getId(), bookId);

        if (alreadyReviewed) {
            throw new BadRequestException("You have already reviewed this book");
        }

        Review review = Review.builder().user(user).book(book).rating(request.rating())
                .comment(normalizeComment(request.comment())).build();

        Review savedReview = reviewRepository.save(review);

        return ReviewMapper.toResponse(savedReview);
    }

    @Transactional
    public ReviewResponse updateOwnReview(String email, Long bookId, UpdateReviewRequest request) {

        User user = getUserByEmail(email);

        getPublishedBook(bookId);

        Review review = reviewRepository.findByUser_IdAndBook_Id(user.getId(), bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        review.setRating(request.rating());

        review.setComment(normalizeComment(request.comment()));

        Review updatedReview = reviewRepository.save(review);

        return ReviewMapper.toResponse(updatedReview);
    }

    @Transactional
    public void deleteOwnReview(String email, Long bookId) {

        User user = getUserByEmail(email);

        getPublishedBook(bookId);

        Review review = reviewRepository.findByUser_IdAndBook_Id(user.getId(), bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getBookReviews(Long bookId, ReviewSort sort, int page, int size) {

        getPublishedBook(bookId);
        validatePagination(page, size);

        Pageable pageable = PageRequest.of(page, size, getReviewSort(sort));

        Page<Review> reviewPage = reviewRepository.findAllByBook_Id(bookId, pageable);

        Page<ReviewResponse> responsePage = reviewPage.map(ReviewMapper::toResponse);

        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse getReviewSummary(Long bookId) {

        getPublishedBook(bookId);

        long reviewCount = reviewRepository.countByBook_Id(bookId);

        Double average = reviewRepository.findAverageRatingByBookId(bookId);

        double averageRating = average == null ? 0.0 : roundToOneDecimal(average);

        return new ReviewSummaryResponse(averageRating, reviewCount);
    }

    private Sort getReviewSort(ReviewSort reviewSort) {

        ReviewSort selectedSort = reviewSort == null ? ReviewSort.NEWEST : reviewSort;

        return switch (selectedSort) {

        case NEWEST -> Sort.by(Sort.Direction.DESC, "createdAt");

        case OLDEST -> Sort.by(Sort.Direction.ASC, "createdAt");

        case HIGHEST_RATING -> Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.DESC, "createdAt"));

        case LOWEST_RATING -> Sort.by(Sort.Direction.ASC, "rating").and(Sort.by(Sort.Direction.DESC, "createdAt"));
        };
    }

    private Book getPublishedBook(Long bookId) {

        return bookRepository.findByIdAndStatus(bookId, BookStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
    }

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String normalizeComment(String comment) {

        if (comment == null || comment.isBlank()) {

            return null;
        }

        return comment.trim();
    }

    private void validatePagination(int page, int size) {

        if (page < 0) {
            throw new BadRequestException("Page number must be 0 or greater");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }
    }

    private double roundToOneDecimal(double value) {

        return Math.round(value * 10.0) / 10.0;
    }
}