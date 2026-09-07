package com.readora.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateReadingProgressRequest(

        @NotNull(message = "Current page is required") @Positive(message = "Current page must be greater than 0") Integer currentPage,

        @NotNull(message = "Total pages is required") @Positive(message = "Total pages must be greater than 0") Integer totalPages

) {
}
