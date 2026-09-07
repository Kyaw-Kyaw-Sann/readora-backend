package com.readora.backend.mapper;

import com.readora.backend.dto.response.AdminReviewBookResponse;
import com.readora.backend.dto.response.AdminReviewResponse;
import com.readora.backend.dto.response.AdminReviewUserResponse;
import com.readora.backend.entity.Book;
import com.readora.backend.entity.Review;
import com.readora.backend.entity.User;

public final class AdminReviewMapper {

    private AdminReviewMapper() {
    }

    public static AdminReviewResponse toResponse(Review review) {

        return new AdminReviewResponse(review.getId(), toUserResponse(review.getUser()),
                toBookResponse(review.getBook()), review.getRating(), review.getComment(), review.getCreatedAt(),
                review.getUpdatedAt());
    }

    private static AdminReviewUserResponse toUserResponse(User user) {

        return new AdminReviewUserResponse(user.getId(), user.getName(), user.getEmail(), user.getProfileImageUrl());
    }

    private static AdminReviewBookResponse toBookResponse(Book book) {

        return new AdminReviewBookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getCoverUrl());
    }
}
