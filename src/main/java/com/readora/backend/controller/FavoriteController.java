package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.response.BookPublicSummaryResponse;
import com.readora.backend.dto.response.FavoriteStatusResponse;
import com.readora.backend.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "Add a book to favorites")
    @PostMapping("/{bookId}")
    public ResponseEntity<ApiResponse<BookPublicSummaryResponse>> addFavorite(

            @PathVariable Long bookId,

            @AuthenticationPrincipal UserDetails userDetails) {

        BookPublicSummaryResponse response = favoriteService.addFavorite(userDetails.getUsername(), bookId);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Book added to favorites", response));
    }

    @Operation(summary = "Remove a book from favorites")
    @DeleteMapping("/{bookId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(

            @PathVariable Long bookId,

            @AuthenticationPrincipal UserDetails userDetails) {

        favoriteService.removeFavorite(userDetails.getUsername(), bookId);

        return ResponseEntity.ok(ApiResponse.success("Book removed from favorites"));
    }

    @Operation(summary = "Check favorite status")
    @GetMapping("/{bookId}/status")
    public ResponseEntity<ApiResponse<FavoriteStatusResponse>> getFavoriteStatus(

            @PathVariable Long bookId,

            @AuthenticationPrincipal UserDetails userDetails) {

        FavoriteStatusResponse response = favoriteService.getFavoriteStatus(userDetails.getUsername(), bookId);

        return ResponseEntity.ok(ApiResponse.success("Favorite status retrieved successfully", response));
    }

    @Operation(summary = "Get current user's favorite books")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookPublicSummaryResponse>>> getFavorites(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        PageResponse<BookPublicSummaryResponse> response = favoriteService.getFavorites(userDetails.getUsername(), page,
                size);

        return ResponseEntity.ok(ApiResponse.success("Favorite books retrieved successfully", response));
    }
}
