package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.request.UpdateReadingProgressRequest;
import com.readora.backend.dto.response.ReadingProgressResponse;
import com.readora.backend.service.ReadingProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reading-progress")
@RequiredArgsConstructor
@Tag(name = "Reading Progress")
public class ReadingProgressController {

    private final ReadingProgressService readingProgressService;

    @Operation(summary = "Get or resume reading progress")
    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<ReadingProgressResponse>> getProgress(

            @PathVariable Long bookId,

            @AuthenticationPrincipal UserDetails userDetails) {

        ReadingProgressResponse response = readingProgressService.getProgress(userDetails.getUsername(), bookId);

        return ResponseEntity.ok(ApiResponse.success("Reading progress retrieved successfully", response));
    }

    @Operation(summary = "Update reading progress")
    @PutMapping("/{bookId}")
    public ResponseEntity<ApiResponse<ReadingProgressResponse>> updateProgress(

            @PathVariable Long bookId,

            @Valid @RequestBody UpdateReadingProgressRequest request,

            @AuthenticationPrincipal UserDetails userDetails) {

        ReadingProgressResponse response = readingProgressService.updateProgress(userDetails.getUsername(), bookId,
                request);

        return ResponseEntity.ok(ApiResponse.success("Reading progress updated successfully", response));
    }

    @Operation(summary = "Get continue reading books")
    @GetMapping("/continue")
    public ResponseEntity<ApiResponse<PageResponse<ReadingProgressResponse>>> getContinueReading(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        PageResponse<ReadingProgressResponse> response = readingProgressService
                .getContinueReading(userDetails.getUsername(), page, size);

        return ResponseEntity.ok(ApiResponse.success("Continue reading books retrieved successfully", response));
    }

    @Operation(summary = "Get recently read books")
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<PageResponse<ReadingProgressResponse>>> getRecentlyRead(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        PageResponse<ReadingProgressResponse> response = readingProgressService
                .getRecentlyRead(userDetails.getUsername(), page, size);

        return ResponseEntity.ok(ApiResponse.success("Recently read books retrieved successfully", response));
    }
}
