package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.response.RecommendationResponse;
import com.readora.backend.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(summary = "Get personalized book recommendations")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RecommendationResponse>>> getRecommendations(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        PageResponse<RecommendationResponse> response = recommendationService
                .getRecommendations(userDetails.getUsername(), page, size);

        return ResponseEntity.ok(ApiResponse.success("Recommendations retrieved successfully", response));
    }
}