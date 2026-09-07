package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.request.UpdateListeningProgressRequest;
import com.readora.backend.dto.response.ListeningProgressResponse;
import com.readora.backend.service.ListeningProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listening-progress")
@RequiredArgsConstructor
@Tag(name = "Listening Progress")
public class ListeningProgressController {

    private final ListeningProgressService listeningProgressService;

    @Operation(summary = "Get or resume listening progress")
    @GetMapping("/{bookId}")
    public ResponseEntity<ApiResponse<ListeningProgressResponse>> getProgress(

            @PathVariable Long bookId,

            @AuthenticationPrincipal UserDetails userDetails) {

        ListeningProgressResponse response = listeningProgressService.getProgress(userDetails.getUsername(), bookId);

        return ResponseEntity.ok(ApiResponse.success("Listening progress retrieved successfully", response));
    }

    @Operation(summary = "Update listening progress")
    @PutMapping("/{bookId}")
    public ResponseEntity<ApiResponse<ListeningProgressResponse>> updateProgress(

            @PathVariable Long bookId,

            @Valid @RequestBody UpdateListeningProgressRequest request,

            @AuthenticationPrincipal UserDetails userDetails) {

        ListeningProgressResponse response = listeningProgressService.updateProgress(userDetails.getUsername(), bookId,
                request);

        return ResponseEntity.ok(ApiResponse.success("Listening progress updated successfully", response));
    }

    @Operation(summary = "Get continue listening books")
    @GetMapping("/continue")
    public ResponseEntity<ApiResponse<PageResponse<ListeningProgressResponse>>> getContinueListening(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        PageResponse<ListeningProgressResponse> response = listeningProgressService
                .getContinueListening(userDetails.getUsername(), page, size);

        return ResponseEntity.ok(ApiResponse.success("Continue listening books retrieved successfully", response));
    }

    @Operation(summary = "Get recently listened books")
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<PageResponse<ListeningProgressResponse>>> getRecentlyListened(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size,

            @AuthenticationPrincipal UserDetails userDetails) {

        PageResponse<ListeningProgressResponse> response = listeningProgressService
                .getRecentlyListened(userDetails.getUsername(), page, size);

        return ResponseEntity.ok(ApiResponse.success("Recently listened books retrieved successfully", response));
    }
}