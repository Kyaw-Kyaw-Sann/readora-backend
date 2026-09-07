package com.readora.backend.service;

import com.readora.backend.entity.EmailVerificationToken;
import com.readora.backend.entity.User;
import com.readora.backend.enums.AuthProvider;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.exception.ResourceNotFoundException;
import com.readora.backend.repository.EmailVerificationTokenRepository;
import com.readora.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${email-verification.expiration-hours}")
    private long expirationHours;

    @Transactional
    public void sendVerificationEmail(User user) {

        invalidatePreviousTokens(user);

        String token = generateSecureToken();

        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .user(user)
                .token(token)
                .expiresAt(
                        LocalDateTime.now()
                                .plusHours(expirationHours))
                .used(false)
                .build();

        tokenRepository.save(verificationToken);

        String verificationLink = baseUrl
                + "/api/auth/verify-email?token="
                + token;

        emailService.sendVerificationEmail(
                user.getEmail(),
                verificationLink);
    }

    @Transactional
    public void verifyEmail(String token) {

        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException(
                        "Invalid verification token"));

        if (verificationToken.isUsed()) {
            throw new BadRequestException(
                    "Verification token has already been used");
        }

        if (verificationToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new BadRequestException(
                    "Verification token has expired");
        }

        User user = verificationToken.getUser();

        if (user.isEmailVerified()) {
            throw new BadRequestException(
                    "Email is already verified");
        }

        user.setEmailVerified(true);
        verificationToken.setUsed(true);

        userRepository.save(user);
        tokenRepository.save(verificationToken);
    }

    @Transactional
    public void resendVerification(String email) {

        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException(
                    "Email verification is not required for this account");
        }

        if (user.isEmailVerified()) {
            throw new BadRequestException(
                    "Email is already verified");
        }

        sendVerificationEmail(user);
    }

    private void invalidatePreviousTokens(User user) {

        var previousTokens = tokenRepository.findAllByUserAndUsedFalse(user);

        for (EmailVerificationToken token : previousTokens) {
            token.setUsed(true);
        }

        tokenRepository.saveAll(previousTokens);
    }

    private String generateSecureToken() {

        byte[] bytes = new byte[32];

        new SecureRandom().nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
