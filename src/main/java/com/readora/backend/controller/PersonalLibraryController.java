package com.readora.backend.controller;

import com.readora.backend.common.response.ApiResponse;
import com.readora.backend.dto.response.PersonalLibrarySummaryResponse;
import com.readora.backend.service.PersonalLibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
@Tag(name = "Personal Library")
public class PersonalLibraryController {

    private final PersonalLibraryService personalLibraryService;

    @Operation(summary = "Get personal library summary")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PersonalLibrarySummaryResponse>> getLibrarySummary(

            @AuthenticationPrincipal UserDetails userDetails) {

        PersonalLibrarySummaryResponse response = personalLibraryService.getLibrarySummary(userDetails.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Personal library summary retrieved successfully", response));
    }
}
