package com.readora.backend.dto.response;

import com.readora.backend.enums.AuthProvider;
import com.readora.backend.enums.Role;

public record UserResponse(
        Long id,
        String name,
        String email,
        AuthProvider provider,
        boolean emailVerified,
        Role role) {
}
