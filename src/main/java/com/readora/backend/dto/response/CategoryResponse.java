package com.readora.backend.dto.response;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        boolean active) {
}
