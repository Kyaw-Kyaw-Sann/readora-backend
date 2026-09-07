package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.dto.response.BookMediaAccessResponse;
import com.readora.backend.service.BookAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Book Access")
public class BookAccessController {

    private final BookAccessService bookAccessService;

    @Operation(summary = "Get authorized PDF access")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<ApiResponse<BookMediaAccessResponse>> getPdfAccess(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        BookMediaAccessResponse response = bookAccessService.getPdfAccess(userDetails.getUsername(), id);

        return ResponseEntity.ok(ApiResponse.success("PDF access granted", response));
    }

    @Operation(summary = "Get authorized audio access")
    @GetMapping("/{id}/audio")
    public ResponseEntity<ApiResponse<BookMediaAccessResponse>> getAudioAccess(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        BookMediaAccessResponse response = bookAccessService.getAudioAccess(userDetails.getUsername(), id);

        return ResponseEntity.ok(ApiResponse.success("Audio access granted", response));
    }
}
