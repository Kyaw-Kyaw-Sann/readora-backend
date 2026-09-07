package com.readora.backend.service;

import com.readora.backend.dto.request.LoginRequest;
import com.readora.backend.dto.request.LogoutRequest;
import com.readora.backend.dto.request.RefreshTokenRequest;
import com.readora.backend.dto.request.RegisterRequest;
import com.readora.backend.dto.response.LoginResponse;
import com.readora.backend.dto.response.RefreshTokenResponse;
import com.readora.backend.dto.response.UserResponse;
import com.readora.backend.entity.RefreshToken;
import com.readora.backend.entity.User;
import com.readora.backend.enums.AuthProvider;
import com.readora.backend.enums.Role;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.exception.UnauthorizedException;
import com.readora.backend.mapper.UserMapper;
import com.readora.backend.repository.RefreshTokenRepository;
import com.readora.backend.repository.UserRepository;
import com.readora.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;
    private final RefreshTokenService refreshTokenService;

    @Value("${refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public UserResponse register(RegisterRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .name(request.name().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .provider(AuthProvider.LOCAL)
                .emailVerified(false)
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        emailVerificationService.sendVerificationEmail(savedUser);

        return UserMapper.toResponse(savedUser);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(
                        "Invalid email or password"));

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException(
                    "Please sign in using Google");
        }

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.password()));

        } catch (AuthenticationException ex) {

            throw new UnauthorizedException(
                    "Invalid email or password");
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

        String accessToken = jwtService.generateAccessToken(userDetails);

        RefreshToken refreshToken = refreshTokenService.create(user);

        return new LoginResponse(
                accessToken,
                refreshToken.getToken(),
                UserMapper.toResponse(user));
    }

    @Transactional(readOnly = true)
    public RefreshTokenResponse refreshAccessToken(
            RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.refreshToken())
                .orElseThrow(() -> new UnauthorizedException(
                        "Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new UnauthorizedException(
                    "Refresh token has been revoked");
        }

        if (refreshToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new UnauthorizedException(
                    "Refresh token has expired");
        }

        User user = refreshToken.getUser();

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword() != null
                        ? user.getPassword()
                        : "")
                .roles(user.getRole().name())
                .build();

        String accessToken = jwtService.generateAccessToken(userDetails);

        return new RefreshTokenResponse(accessToken);
    }

    @Transactional
    public void logout(LogoutRequest request) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.refreshToken())
                .orElseThrow(() -> new UnauthorizedException(
                        "Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            return;
        }

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }

}