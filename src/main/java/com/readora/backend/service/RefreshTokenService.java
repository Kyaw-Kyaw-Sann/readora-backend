package com.readora.backend.service;

import com.readora.backend.entity.RefreshToken;
import com.readora.backend.entity.User;
import com.readora.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${refresh-token.expiration-days}")
    private long expirationDays;

    @Transactional
    public RefreshToken create(User user) {

        String token = generateSecureToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(
                        LocalDateTime.now()
                                .plusDays(expirationDays))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    private String generateSecureToken() {

        byte[] bytes = new byte[32];

        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}