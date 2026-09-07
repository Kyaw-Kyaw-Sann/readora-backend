package com.readora.backend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.readora.backend.dto.request.GoogleAuthRequest;
import com.readora.backend.dto.response.LoginResponse;
import com.readora.backend.entity.RefreshToken;
import com.readora.backend.entity.User;
import com.readora.backend.enums.AuthProvider;
import com.readora.backend.enums.Role;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.exception.UnauthorizedException;
import com.readora.backend.mapper.UserMapper;
import com.readora.backend.repository.UserRepository;
import com.readora.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public LoginResponse authenticate(
            GoogleAuthRequest request) {

        GoogleIdToken googleIdToken;

        try {

            googleIdToken = googleIdTokenVerifier.verify(
                    request.idToken());

        } catch (Exception ex) {

            throw new UnauthorizedException(
                    "Invalid Google ID token");
        }

        if (googleIdToken == null) {
            throw new UnauthorizedException(
                    "Invalid Google ID token");
        }

        GoogleIdToken.Payload payload = googleIdToken.getPayload();

        String email = payload.getEmail();

        Boolean emailVerified = payload.getEmailVerified();

        if (email == null
                || !Boolean.TRUE.equals(emailVerified)) {

            throw new UnauthorizedException(
                    "Google email is not verified");
        }

        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository
                .findByEmail(normalizedEmail)
                .map(existing -> handleExistingUser(existing))
                .orElseGet(() -> createGoogleUser(payload));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password("")
                .roles(user.getRole().name())
                .build();

        String accessToken = jwtService.generateAccessToken(userDetails);

        RefreshToken refreshToken = refreshTokenService.create(user);

        return new LoginResponse(
                accessToken,
                refreshToken.getToken(),
                UserMapper.toResponse(user));
    }

    private User handleExistingUser(User user) {

        if (user.getProvider() != AuthProvider.GOOGLE) {

            throw new BadRequestException(
                    "An account already exists with this email. "
                            + "Please sign in with email and password.");
        }

        return user;
    }

    private User createGoogleUser(
            GoogleIdToken.Payload payload) {

        String email = payload.getEmail()
                .trim()
                .toLowerCase();

        String name = (String) payload.get("name");

        String picture = (String) payload.get("picture");

        if (name == null || name.isBlank()) {
            name = email.split("@")[0];
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(null)
                .provider(AuthProvider.GOOGLE)
                .emailVerified(true)
                .role(Role.USER)
                .profileImageUrl(picture)
                .build();

        return userRepository.save(user);
    }
}