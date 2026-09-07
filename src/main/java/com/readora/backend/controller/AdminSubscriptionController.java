package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.response.AdminSubscriptionResponse;
import com.readora.backend.enums.SubscriptionPlan;
import com.readora.backend.enums.SubscriptionStatus;
import com.readora.backend.service.AdminSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Admin Subscriptions")
public class AdminSubscriptionController {

    private final AdminSubscriptionService adminSubscriptionService;

    @Operation(summary = "Get subscriptions with admin filters")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminSubscriptionResponse>>> getSubscriptions(

            @RequestParam(required = false) String search,

            @RequestParam(required = false) SubscriptionStatus status,

            @RequestParam(required = false) SubscriptionPlan plan,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size) {

        PageResponse<AdminSubscriptionResponse> response = adminSubscriptionService.getSubscriptions(search, status,
                plan, page, size);

        return ResponseEntity.ok(ApiResponse.success("Subscriptions retrieved successfully", response));
    }
}