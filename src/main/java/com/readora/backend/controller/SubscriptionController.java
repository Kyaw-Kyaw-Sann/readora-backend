package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.dto.request.CreateSubscriptionRequest;
import com.readora.backend.dto.response.SubscriptionResponse;
import com.readora.backend.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(summary = "Get current active subscription")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getCurrentSubscription(
            @AuthenticationPrincipal UserDetails userDetails) {

        SubscriptionResponse subscription = subscriptionService.getCurrentSubscription(userDetails.getUsername());

        if (subscription == null) {
            return ResponseEntity.ok(ApiResponse.success("No active subscription"));
        }

        return ResponseEntity.ok(ApiResponse.success("Subscription retrieved successfully", subscription));
    }

    @Operation(summary = "Create a mock subscription")
    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody CreateSubscriptionRequest request) {

        SubscriptionResponse subscription = subscriptionService.createSubscription(userDetails.getUsername(), request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription activated successfully", subscription));
    }

    @Operation(summary = "Cancel current active subscription")
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancelSubscription(
            @AuthenticationPrincipal UserDetails userDetails) {

        SubscriptionResponse subscription = subscriptionService.cancelSubscription(userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled successfully", subscription));
    }

    @Operation(summary = "Get subscription history")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getSubscriptionHistory(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<SubscriptionResponse> history = subscriptionService.getSubscriptionHistory(userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Subscription history retrieved successfully", history));
    }
}
