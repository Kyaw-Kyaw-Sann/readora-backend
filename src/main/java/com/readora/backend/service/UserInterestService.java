package com.readora.backend.service;

import com.readora.backend.dto.request.UpdateUserInterestsRequest;
import com.readora.backend.dto.response.CategoryResponse;
import com.readora.backend.entity.Category;
import com.readora.backend.entity.User;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.exception.ResourceNotFoundException;
import com.readora.backend.mapper.CategoryMapper;
import com.readora.backend.repository.CategoryRepository;
import com.readora.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserInterestService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCurrentUserInterests(String email) {
        User user = getUserByEmail(email);

        return user.getInterests()
                .stream()
                .filter(Category::isActive)
                .sorted((first, second) -> first.getName().compareToIgnoreCase(second.getName()))
                .map(CategoryMapper::toResponse)
                .toList();
    }

    @Transactional
    public List<CategoryResponse> updateInterests(
            String email,
            UpdateUserInterestsRequest request) {

        User user = getUserByEmail(email);

        List<Long> categoryIds = request.categoryIds();

        validateDuplicateCategoryIds(categoryIds);

        List<Category> categories = categoryRepository.findAllById(categoryIds);

        validateCategoriesExist(categoryIds, categories);
        validateCategoriesAreActive(categories);

        user.setInterests(new HashSet<>(categories));

        userRepository.save(user);

        return categories.stream()
                .sorted((first, second) -> first.getName().compareToIgnoreCase(second.getName()))
                .map(CategoryMapper::toResponse)
                .toList();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateDuplicateCategoryIds(List<Long> categoryIds) {
        Set<Long> uniqueCategoryIds = new HashSet<>(categoryIds);

        if (uniqueCategoryIds.size() != categoryIds.size()) {
            throw new BadRequestException(
                    "Duplicate category IDs are not allowed");
        }
    }

    private void validateCategoriesExist(
            List<Long> categoryIds,
            List<Category> categories) {

        if (categories.size() != categoryIds.size()) {
            throw new BadRequestException(
                    "One or more categories not found");
        }
    }

    private void validateCategoriesAreActive(List<Category> categories) {
        boolean hasInactiveCategory = categories.stream()
                .anyMatch(category -> !category.isActive());

        if (hasInactiveCategory) {
            throw new BadRequestException(
                    "Inactive categories cannot be selected");
        }
    }
}
