package com.readora.backend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateUserInterestsRequest(

        @NotEmpty(message = "At least one interest must be selected") List<@NotNull(message = "Category ID cannot be null") Long> categoryIds

) {
}
