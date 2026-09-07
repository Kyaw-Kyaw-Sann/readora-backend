package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.common.response.PageResponse;
import com.readora.backend.dto.response.AdminUserDetailResponse;
import com.readora.backend.dto.response.AdminUserSummaryResponse;
import com.readora.backend.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin Users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "Get users with admin filters")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminUserSummaryResponse>>> getUsers(

            @RequestParam(required = false) String search,

            @RequestParam(required = false) Boolean verified,

            @RequestParam(required = false) Boolean premium,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "20") int size) {

        PageResponse<AdminUserSummaryResponse> response = adminUserService.getUsers(search, verified, premium, page,
                size);

        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", response));
    }

    @Operation(summary = "Get user details by admin")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<AdminUserDetailResponse>> getUserDetail(

            @PathVariable Long userId) {

        AdminUserDetailResponse response = adminUserService.getUserDetail(userId);

        return ResponseEntity.ok(ApiResponse.success("User details retrieved successfully", response));
    }
}