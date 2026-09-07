package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.dto.request.CreateCategoryRequest;
import com.readora.backend.dto.request.UpdateCategoryRequest;
import com.readora.backend.dto.response.CategoryResponse;
import com.readora.backend.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Admin Categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Create a new category")
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {

        CategoryResponse category = categoryService.createCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Category created successfully",
                        category));
    }

    @Operation(summary = "Update a category")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest request) {

        CategoryResponse category = categoryService.updateCategory(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category updated successfully",
                        category));
    }

    @Operation(summary = "Deactivate a category")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateCategory(
            @PathVariable Long id) {

        categoryService.deactivateCategory(id);

        return ResponseEntity.ok(
                ApiResponse.success("Category deactivated successfully"));
    }
}