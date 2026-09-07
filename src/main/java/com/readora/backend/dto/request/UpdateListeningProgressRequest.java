package com.readora.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateListeningProgressRequest(

        @NotNull(message = "Current seconds is required") @PositiveOrZero(message = "Current seconds must be 0 or greater") Integer currentSeconds,

        @NotNull(message = "Duration seconds is required") @Positive(message = "Duration seconds must be greater than 0") Integer durationSeconds

) {
}