package com.readora.backend.dto.response;

import com.readora.backend.enums.AuthProvider;
import com.readora.backend.enums.Role;

import java.time.LocalDateTime;
import java.util.List;

public record PersonalLibraryProfileSummaryResponse(Long id, String name, String email, String profileImageUrl,
        AuthProvider provider, boolean emailVerified, Role role, List<String> interests, LocalDateTime createdAt) {
}
