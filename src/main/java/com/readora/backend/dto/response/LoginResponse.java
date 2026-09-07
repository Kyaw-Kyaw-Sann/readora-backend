package com.readora.backend.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UserResponse user) {
}