package com.example.community.domain.admin.dto.response;

import com.example.community.domain.user.entity.User;

import java.time.LocalDateTime;

public record PendingUserResponse(
    Long userId,
    Long studentId,
    String name,
    String school,
    String certificateUrl,
    LocalDateTime createdAt
) {
    public static PendingUserResponse from(User user) {
        return new PendingUserResponse(
            user.getId(), user.getStudentId(), user.getName(),
            user.getSchool(), user.getCertificateUrl(), user.getCreatedAt()
        );
    }
}
