package com.example.community.api.user.dto;

import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.enums.UserRole;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        Long userId,
        Long studentId,
        UserRole role
) {

    public static LoginResponse of(
            String accessToken,
            long expiresInSeconds,
            User user
    ) {
        return new LoginResponse(
                accessToken,
                "Bearer",
                expiresInSeconds,
                user.getId(),
                user.getStudentId(),
                user.getRole()
        );
    }
}