package com.readora.backend.service;

import com.readora.backend.dto.request.CreateCategoryRequest;
import com.readora.backend.dto.request.UpdateCategoryRequest;
import com.readora.backend.dto.response.CategoryResponse;
import com.readora.backend.entity.Category;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.exception.ResourceNotFoundException;
import com.readora.backend.mapper.CategoryMapper;
import com.readora.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(CategoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getActiveCategory(Long id) {
        Category category = categoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        return CategoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        String name = request.name().trim();

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Category name already exists");
        }

        String slug = generateSlug(name);

        if (categoryRepository.existsBySlug(slug)) {
            throw new BadRequestException("Category slug already exists");
        }

        Category category = Category.builder()
                .name(name)
                .slug(slug)
                .description(normalizeDescription(request.description()))
                .active(true)
                .build();

        Category savedCategory = categoryRepository.save(category);

        return CategoryMapper.toResponse(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        String name = request.name().trim();

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BadRequestException("Category name already exists");
        }

        String slug = generateSlug(name);

        if (categoryRepository.existsBySlugAndIdNot(slug, id)) {
            throw new BadRequestException("Category slug already exists");
        }

        category.setName(name);
        category.setSlug(slug);
        category.setDescription(normalizeDescription(request.description()));
        category.setActive(request.active());

        return CategoryMapper.toResponse(category);
    }

    @Transactional
    public void deactivateCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.isActive()) {
            throw new BadRequestException("Category is already inactive");
        }

        category.setActive(false);
    }

    private String generateSlug(String name) {
        return name
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }
}
