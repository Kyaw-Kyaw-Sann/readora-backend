package com.readora.backend.service;

import com.readora.backend.dto.request.ForgotPasswordRequest;
import com.readora.backend.dto.request.ResetPasswordRequest;
import com.readora.backend.dto.request.VerifyResetOtpRequest;
import com.readora.backend.entity.PasswordResetOtp;
import com.readora.backend.entity.RefreshToken;
import com.readora.backend.entity.User;
import com.readora.backend.enums.AuthProvider;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.repository.PasswordResetOtpRepository;
import com.readora.backend.repository.RefreshTokenRepository;
import com.readora.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${password-reset.otp-expiration-minutes}")
    private long otpExpirationMinutes;

    @Value("${password-reset.max-attempts}")
    private int maxAttempts;

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        String email = normalizeEmail(request.email());

        User user = userRepository.findByEmail(email)
                .orElse(null);

        // Generic response from controller:
        // Do not reveal whether the account exists.
        if (user == null) {
            return;
        }

        // Google account does not use traditional password reset.
        // Keep response generic.
        if (user.getProvider() != AuthProvider.LOCAL) {
            return;
        }

        invalidateOldOtps(user);

        String rawOtp = generateOtp();

        PasswordResetOtp resetOtp = PasswordResetOtp.builder()
                .user(user)
                .otp(passwordEncoder.encode(rawOtp))
                .expiresAt(
                        LocalDateTime.now()
                                .plusMinutes(otpExpirationMinutes))
                .used(false)
                .attemptCount(0)
                .build();

        otpRepository.save(resetOtp);

        emailService.sendPasswordResetOtp(
                user.getEmail(),
                rawOtp);
    }

    @Transactional
    public void verifyOtp(VerifyResetOtpRequest request) {

        User user = getLocalUser(request.email());

        PasswordResetOtp resetOtp = getActiveOtp(user);

        validateOtp(resetOtp, request.otp());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        User user = getLocalUser(request.email());

        PasswordResetOtp resetOtp = getActiveOtp(user);

        validateOtp(resetOtp, request.otp());

        user.setPassword(
                passwordEncoder.encode(request.newPassword()));

        resetOtp.setUsed(true);

        userRepository.save(user);
        otpRepository.save(resetOtp);

        revokeRefreshTokens(user);
    }

    private PasswordResetOtp getActiveOtp(User user) {

        return otpRepository
                .findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BadRequestException(
                        "Invalid or expired reset request"));
    }

    private void validateOtp(
            PasswordResetOtp resetOtp,
            String rawOtp) {

        if (resetOtp.isUsed()) {
            throw new BadRequestException(
                    "OTP has already been used");
        }

        if (resetOtp.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            resetOtp.setUsed(true);
            otpRepository.save(resetOtp);

            throw new BadRequestException(
                    "OTP has expired");
        }

        if (resetOtp.getAttemptCount() >= maxAttempts) {

            resetOtp.setUsed(true);
            otpRepository.save(resetOtp);

            throw new BadRequestException(
                    "Too many invalid OTP attempts");
        }

        if (!passwordEncoder.matches(
                rawOtp,
                resetOtp.getOtp())) {

            resetOtp.setAttemptCount(
                    resetOtp.getAttemptCount() + 1);

            if (resetOtp.getAttemptCount() >= maxAttempts) {
                resetOtp.setUsed(true);
            }

            otpRepository.save(resetOtp);

            throw new BadRequestException(
                    "Invalid OTP");
        }
    }

    private User getLocalUser(String email) {

        String normalizedEmail = normalizeEmail(email);

        User user = userRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException(
                        "Invalid reset request"));

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException(
                    "Password reset is not available for this account");
        }

        return user;
    }

    private void invalidateOldOtps(User user) {

        List<PasswordResetOtp> oldOtps = otpRepository.findAllByUserAndUsedFalse(user);

        for (PasswordResetOtp otp : oldOtps) {
            otp.setUsed(true);
        }

        otpRepository.saveAll(oldOtps);
    }

    private void revokeRefreshTokens(User user) {

        List<RefreshToken> refreshTokens = refreshTokenRepository
                .findAllByUserAndRevokedFalse(user);

        for (RefreshToken refreshToken : refreshTokens) {
            refreshToken.setRevoked(true);
        }

        refreshTokenRepository.saveAll(refreshTokens);
    }

    private String generateOtp() {

        int number = secureRandom.nextInt(1_000_000);

        return String.format("%06d", number);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
