package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.dto.request.ForgotPasswordRequest;
import com.readora.backend.dto.request.GoogleAuthRequest;
import com.readora.backend.dto.request.LoginRequest;
import com.readora.backend.dto.request.LogoutRequest;
import com.readora.backend.dto.request.RefreshTokenRequest;
import com.readora.backend.dto.request.RegisterRequest;
import com.readora.backend.dto.request.ResendVerificationRequest;
import com.readora.backend.dto.request.ResetPasswordRequest;
import com.readora.backend.dto.request.VerifyResetOtpRequest;
import com.readora.backend.dto.response.LoginResponse;
import com.readora.backend.dto.response.RefreshTokenResponse;
import com.readora.backend.dto.response.UserResponse;
import com.readora.backend.service.AuthService;
import com.readora.backend.service.EmailVerificationService;
import com.readora.backend.service.GoogleAuthService;
import com.readora.backend.service.PasswordResetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;
    private final GoogleAuthService googleAuthService;

    @Operation(summary = "Register a new traditional user")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse user = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Registration successful",
                                user));
    }

    @Operation(summary = "Login with email and password")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Login successful",
                        response));
    }

    @Operation(summary = "Generate a new access token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        RefreshTokenResponse response = authService.refreshAccessToken(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Access token refreshed successfully",
                        response));
    }

    @Operation(summary = "Logout user")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request) {

        authService.logout(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Logout successful"));
    }

    @Operation(summary = "Verify user email")
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @RequestParam String token) {

        emailVerificationService.verifyEmail(token);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Email verified successfully"));
    }

    @Operation(summary = "Resend verification email")
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {

        emailVerificationService.resendVerification(
                request.email());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Verification email sent successfully"));
    }

    @Operation(summary = "Request password reset OTP")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordResetService.forgotPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "If an account exists, a reset OTP has been sent"));
    }

    @Operation(summary = "Verify password reset OTP")
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<Void>> verifyResetOtp(
            @Valid @RequestBody VerifyResetOtpRequest request) {

        passwordResetService.verifyOtp(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "OTP verified successfully"));
    }

    @Operation(summary = "Reset user password")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Password reset successfully"));
    }

    @Operation(summary = "Authenticate using Google")
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponse>> googleLogin(
            @Valid @RequestBody GoogleAuthRequest request) {

        LoginResponse response = googleAuthService.authenticate(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Google authentication successful",
                        response));
    }
}