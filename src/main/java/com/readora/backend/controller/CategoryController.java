package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.dto.response.CategoryResponse;
import com.readora.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Get all active categories")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {

        List<CategoryResponse> categories = categoryService.getActiveCategories();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Categories retrieved successfully",
                        categories));
    }

    @Operation(summary = "Get active category by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(
            @PathVariable Long id) {

        CategoryResponse category = categoryService.getActiveCategory(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category retrieved successfully",
                        category));
    }
}
