package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.response.BookPublicDetailResponse;
import com.readora.backend.dto.response.BookPublicSummaryResponse;
import com.readora.backend.enums.BookAccessType;
import com.readora.backend.enums.BookSort;
import com.readora.backend.service.BookDiscoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Book Discovery")
public class BookController {

    private final BookDiscoveryService bookDiscoveryService;

    @Operation(summary = "Discover published books")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookPublicSummaryResponse>>> getBooks(

            @RequestParam(required = false) String search,

            @RequestParam(required = false) Long categoryId,

            @RequestParam(required = false) BookAccessType accessType,

            @RequestParam(defaultValue = "NEWEST") BookSort sort,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size) {

        PageResponse<BookPublicSummaryResponse> books = bookDiscoveryService.getBooks(search, categoryId, accessType,
                sort, page, size);

        return ResponseEntity.ok(ApiResponse.success("Books retrieved successfully", books));
    }

    @Operation(summary = "Get published book detail")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookPublicDetailResponse>> getBookDetail(@PathVariable Long id) {

        BookPublicDetailResponse book = bookDiscoveryService.getBookDetail(id);

        return ResponseEntity.ok(ApiResponse.success("Book retrieved successfully", book));
    }

    @Operation(summary = "Get free books")
    @GetMapping("/free")
    public ResponseEntity<ApiResponse<PageResponse<BookPublicSummaryResponse>>> getFreeBooks(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size) {

        PageResponse<BookPublicSummaryResponse> books = bookDiscoveryService.getFreeBooks(page, size);

        return ResponseEntity.ok(ApiResponse.success("Free books retrieved successfully", books));
    }

    @Operation(summary = "Get premium books")
    @GetMapping("/premium")
    public ResponseEntity<ApiResponse<PageResponse<BookPublicSummaryResponse>>> getPremiumBooks(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size) {

        PageResponse<BookPublicSummaryResponse> books = bookDiscoveryService.getPremiumBooks(page, size);

        return ResponseEntity.ok(ApiResponse.success("Premium books retrieved successfully", books));
    }

    @Operation(summary = "Get newly added books")
    @GetMapping("/new")
    public ResponseEntity<ApiResponse<PageResponse<BookPublicSummaryResponse>>> getNewBooks(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size) {

        PageResponse<BookPublicSummaryResponse> books = bookDiscoveryService.getNewBooks(page, size);

        return ResponseEntity.ok(ApiResponse.success("New books retrieved successfully", books));
    }

    @Operation(summary = "Get popular books")
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<PageResponse<BookPublicSummaryResponse>>> getPopularBooks(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size) {

        PageResponse<BookPublicSummaryResponse> books = bookDiscoveryService.getPopularBooks(page, size);

        return ResponseEntity.ok(ApiResponse.success("Popular books retrieved successfully", books));
    }
}