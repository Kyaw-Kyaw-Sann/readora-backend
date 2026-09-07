package com.readora.backend.mapper;

import com.readora.backend.dto.response.ReviewResponse;
import com.readora.backend.dto.response.ReviewUserResponse;
import com.readora.backend.entity.Review;
import com.readora.backend.entity.User;

public final class ReviewMapper {

    private ReviewMapper() {
    }

    public static ReviewResponse toResponse(Review review) {

        return new ReviewResponse(review.getId(), review.getBook().getId(), toUserResponse(review.getUser()),
                review.getRating(), review.getComment(), review.getCreatedAt(), review.getUpdatedAt());
    }

    private static ReviewUserResponse toUserResponse(User user) {

        return new ReviewUserResponse(user.getId(), user.getName(), user.getProfileImageUrl());
    }
}