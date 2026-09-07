package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.dto.request.ChangePasswordRequest;
import com.readora.backend.dto.request.UpdateProfileRequest;
import com.readora.backend.dto.request.UpdateUserInterestsRequest;
import com.readora.backend.dto.response.CategoryResponse;
import com.readora.backend.dto.response.UserProfileResponse;
import com.readora.backend.service.UserInterestService;
import com.readora.backend.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile")
public class UserController {

    private final UserProfileService userProfileService;
    private final UserInterestService userInterestService;

    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUserProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        UserProfileResponse profile = userProfileService.getCurrentUserProfile(userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));
    }

    @Operation(summary = "Update current user profile")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody UpdateProfileRequest request) {

        UserProfileResponse profile = userProfileService.updateProfile(userDetails.getUsername(), request);

        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));
    }

    @Operation(summary = "Change current user password")
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {

        userProfileService.changePassword(userDetails.getUsername(), request);

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @Operation(summary = "Get current user interests")
    @GetMapping("/me/interests")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getInterests(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<CategoryResponse> interests = userInterestService.getCurrentUserInterests(userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success("User interests retrieved successfully", interests));
    }

    @Operation(summary = "Select or update current user interests")
    @PutMapping("/me/interests")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> updateInterests(
            @AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody UpdateUserInterestsRequest request) {

        List<CategoryResponse> interests = userInterestService.updateInterests(userDetails.getUsername(), request);

        return ResponseEntity.ok(ApiResponse.success("User interests updated successfully", interests));
    }
}