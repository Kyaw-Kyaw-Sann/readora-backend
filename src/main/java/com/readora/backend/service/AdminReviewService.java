
package com.readora.backend.service;

import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.response.AdminReviewResponse;
import com.readora.backend.entity.Review;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.exception.ResourceNotFoundException;
import com.readora.backend.mapper.AdminReviewMapper;
import com.readora.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public PageResponse<AdminReviewResponse> getReviews(String search, Integer rating, int page, int size) {

        validatePagination(page, size);
        validateRating(rating);

        String normalizedSearch = normalizeSearch(search);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Review> reviewPage;

        if (normalizedSearch == null) {

            reviewPage = reviewRepository.findAdminReviews(rating, pageable);

        } else {

            String searchPattern = "%" + normalizedSearch + "%";

            reviewPage = reviewRepository.findAdminReviewsBySearch(searchPattern, rating, pageable);
        }

        Page<AdminReviewResponse> responsePage = reviewPage.map(AdminReviewMapper::toResponse);

        return PageResponse.from(responsePage);
    }

    @Transactional
    public void deleteReview(Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        reviewRepository.delete(review);
    }

    private void validateRating(Integer rating) {

        if (rating == null) {
            return;
        }

        if (rating < 1 || rating > 5) {
            throw new BadRequestException("Rating must be between 1 and 5");
        }
    }

    private String normalizeSearch(String search) {

        if (search == null || search.isBlank()) {

            return null;
        }

        return search.trim().toLowerCase();
    }

    private void validatePagination(int page, int size) {

        if (page < 0) {
            throw new BadRequestException("Page number must be 0 or greater");
        }

        if (size < 1 || size > 100) {
            throw new BadRequestException("Page size must be between 1 and 100");
        }
    }
}