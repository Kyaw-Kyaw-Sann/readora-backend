package com.readora.backend.service;

import com.readora.backend.dto.request.ChangePasswordRequest;
import com.readora.backend.dto.request.UpdateProfileRequest;
import com.readora.backend.dto.response.UserProfileResponse;
import com.readora.backend.entity.User;
import com.readora.backend.enums.AuthProvider;
import com.readora.backend.exception.BadRequestException;
import com.readora.backend.exception.ResourceNotFoundException;
import com.readora.backend.mapper.UserProfileMapper;
import com.readora.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionService subscriptionService;

    @Transactional
    public UserProfileResponse getCurrentUserProfile(String email) {
        User user = getUserByEmail(email);

        String subscriptionStatus = subscriptionService.getSubscriptionStatus(user.getId());

        return UserProfileMapper.toResponse(user, subscriptionStatus);
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {

        User user = getUserByEmail(email);

        user.setName(request.name().trim());
        user.setProfileImageUrl(normalizeProfileImageUrl(request.profileImageUrl()));

        User updatedUser = userRepository.save(user);

        String subscriptionStatus = subscriptionService.getSubscriptionStatus(updatedUser.getId());

        return UserProfileMapper.toResponse(updatedUser, subscriptionStatus);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {

        User user = getUserByEmail(email);

        if (user.getProvider() != AuthProvider.LOCAL) {
            throw new BadRequestException("Password change is not available for Google accounts");
        }

        if (user.getPassword() == null) {
            throw new BadRequestException("Password is not available for this account");
        }

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {

            throw new BadRequestException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {

            throw new BadRequestException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));

        userRepository.save(user);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String normalizeProfileImageUrl(String profileImageUrl) {
        if (profileImageUrl == null || profileImageUrl.isBlank()) {
            return null;
        }

        return profileImageUrl.trim();
    }
}