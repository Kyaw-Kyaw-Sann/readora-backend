package com.readora.backend.mapper;

import com.readora.backend.dto.response.CategoryResponse;
import com.readora.backend.entity.Category;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.isActive());
    }
}