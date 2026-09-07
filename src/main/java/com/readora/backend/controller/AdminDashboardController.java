package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.dto.response.AdminDashboardResponse;
import com.readora.backend.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(summary = "Get admin dashboard statistics")
    @GetMapping
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard() {

        AdminDashboardResponse response = adminDashboardService.getDashboard();

        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", response));
    }
}