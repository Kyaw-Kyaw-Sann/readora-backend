package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.response.AdminReviewResponse;
import com.readora.backend.service.AdminReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
@Tag(name = "Admin Reviews")
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @Operation(summary = "Get reviews with admin filters")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminReviewResponse>>> getReviews(

            @RequestParam(required = false) String search,

            @RequestParam(required = false) Integer rating,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size) {

        PageResponse<AdminReviewResponse> response = adminReviewService.getReviews(search, rating, page, size);

        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved successfully", response));
    }

    @Operation(summary = "Delete a review by admin")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(

            @PathVariable Long reviewId) {

        adminReviewService.deleteReview(reviewId);

        return ResponseEntity.ok(ApiResponse.success("Review deleted successfully"));
    }
}
