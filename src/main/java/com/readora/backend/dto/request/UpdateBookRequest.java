package com.readora.backend.dto.request;

import com.readora.backend.enums.BookAccessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record UpdateBookRequest(

        @NotBlank(message = "Title is required") @Size(max = 200, message = "Title must not exceed 200 characters") String title,

        String description,

        @Size(max = 50, message = "ISBN must not exceed 50 characters") String isbn,

        @Size(max = 50, message = "Language must not exceed 50 characters") String language,

        LocalDate publicationDate,

        @NotBlank(message = "Author is required") @Size(max = 150, message = "Author must not exceed 150 characters") String author,

        @Positive(message = "Page count must be greater than 0") Integer pageCount,

        @Positive(message = "Audio duration must be greater than 0") Integer audioDurationSeconds,

        @NotNull(message = "Access type is required") BookAccessType accessType,

        @NotEmpty(message = "At least one category is required") List<@NotNull(message = "Category ID cannot be null") Long> categoryIds,

        boolean removeCover, boolean removePdf, boolean removeAudio

) {
}
