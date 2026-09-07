package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.request.CreateReviewRequest;
import com.readora.backend.dto.request.UpdateReviewRequest;
import com.readora.backend.dto.response.ReviewResponse;
import com.readora.backend.dto.response.ReviewSummaryResponse;
import com.readora.backend.enums.ReviewSort;
import com.readora.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books/{bookId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Create a review for a book")
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(

            @PathVariable Long bookId,

            @Valid @RequestBody CreateReviewRequest request,

            @AuthenticationPrincipal UserDetails userDetails) {

        ReviewResponse response = reviewService.createReview(userDetails.getUsername(), bookId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review created successfully", response));
    }

    @Operation(summary = "Update current user's review")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateOwnReview(

            @PathVariable Long bookId,

            @Valid @RequestBody UpdateReviewRequest request,

            @AuthenticationPrincipal UserDetails userDetails) {

        ReviewResponse response = reviewService.updateOwnReview(userDetails.getUsername(), bookId, request);

        return ResponseEntity.ok(ApiResponse.success("Review updated successfully", response));
    }

    @Operation(summary = "Delete current user's review")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteOwnReview(

            @PathVariable Long bookId,

            @AuthenticationPrincipal UserDetails userDetails) {

        reviewService.deleteOwnReview(userDetails.getUsername(), bookId);

        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully"));
    }

    @Operation(summary = "Get reviews for a book")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getBookReviews(

            @PathVariable Long bookId,

            @RequestParam(defaultValue = "NEWEST") ReviewSort sort,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size) {

        PageResponse<ReviewResponse> response = reviewService.getBookReviews(bookId, sort, page, size);

        return ResponseEntity.ok(ApiResponse.success("Book reviews retrieved successfully", response));
    }

    @Operation(summary = "Get review summary for a book")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ReviewSummaryResponse>> getReviewSummary(@PathVariable Long bookId) {

        ReviewSummaryResponse response = reviewService.getReviewSummary(bookId);

        return ResponseEntity.ok(ApiResponse.success("Review summary retrieved successfully", response));
    }
}