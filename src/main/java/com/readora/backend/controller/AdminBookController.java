package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.request.CreateBookRequest;
import com.readora.backend.dto.request.UpdateBookRequest;
import com.readora.backend.dto.response.BookDetailResponse;
import com.readora.backend.dto.response.BookSummaryResponse;
import com.readora.backend.enums.BookAccessType;
import com.readora.backend.enums.BookStatus;
import com.readora.backend.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/books")
@RequiredArgsConstructor
@Tag(name = "Admin Books")
public class AdminBookController {

    private final BookService bookService;

    @Operation(summary = "Create a new book")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BookDetailResponse>> createBook(

            @Valid @RequestPart("request") CreateBookRequest request,

            @RequestPart(value = "cover", required = false) MultipartFile cover,

            @RequestPart(value = "pdf", required = false) MultipartFile pdf,

            @RequestPart(value = "audio", required = false) MultipartFile audio) {

        BookDetailResponse book = bookService.createBook(request, cover, pdf, audio);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Book created successfully", book));
    }

    @Operation(summary = "Get admin book list")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BookSummaryResponse>>> getBooks(

            @RequestParam(required = false) String search,

            @RequestParam(required = false) BookStatus status,

            @RequestParam(required = false) BookAccessType accessType,

            @RequestParam(required = false) Long categoryId,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size) {

        PageResponse<BookSummaryResponse> books = bookService.getAdminBooks(search, status, accessType, categoryId,
                page, size);

        return ResponseEntity.ok(ApiResponse.success("Books retrieved successfully", books));
    }

    @Operation(summary = "Get book detail for admin")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookDetailResponse>> getBook(@PathVariable Long id) {

        BookDetailResponse book = bookService.getAdminBook(id);

        return ResponseEntity.ok(ApiResponse.success("Book retrieved successfully", book));
    }

    @Operation(summary = "Update a book")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BookDetailResponse>> updateBook(

            @PathVariable Long id,

            @Valid @RequestPart("request") UpdateBookRequest request,

            @RequestPart(value = "cover", required = false) MultipartFile cover,

            @RequestPart(value = "pdf", required = false) MultipartFile pdf,

            @RequestPart(value = "audio", required = false) MultipartFile audio) {

        BookDetailResponse book = bookService.updateBook(id, request, cover, pdf, audio);

        return ResponseEntity.ok(ApiResponse.success("Book updated successfully", book));
    }

    @Operation(summary = "Publish a draft book")
    @PutMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<BookDetailResponse>> publishBook(@PathVariable Long id) {

        BookDetailResponse book = bookService.publishBook(id);

        return ResponseEntity.ok(ApiResponse.success("Book published successfully", book));
    }

    @Operation(summary = "Archive a book")
    @PutMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<BookDetailResponse>> archiveBook(@PathVariable Long id) {

        BookDetailResponse book = bookService.archiveBook(id);

        return ResponseEntity.ok(ApiResponse.success("Book archived successfully", book));
    }
}