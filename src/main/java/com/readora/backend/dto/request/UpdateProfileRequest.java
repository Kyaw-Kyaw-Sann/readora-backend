package com.readora.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(

        @NotBlank(message = "Name is required") @Size(max = 100, message = "Name must not exceed 100 characters") String name,

        @Size(max = 2000, message = "Profile image URL must not exceed 2000 characters") String profileImageUrl

) {
}