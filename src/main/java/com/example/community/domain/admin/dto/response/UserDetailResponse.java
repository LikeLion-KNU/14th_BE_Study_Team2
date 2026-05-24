package com.example.community.domain.admin.dto.response;

import java.time.LocalDateTime;

public record UserDetailResponse(
    Long userId,
    Long studentId,
    String nickname,
    String name,
    String school,
    String status,
    String role,
    LocalDateTime createdAt,
    LocalDateTime approvedAt,
    long postCount,
    long commentCount
) {}
