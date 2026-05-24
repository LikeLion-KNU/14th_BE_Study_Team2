package com.example.community.domain.admin.dto.response;

import java.time.LocalDateTime;

public record ApproveResponse(
    Long userId,
    String status,
    LocalDateTime approvedAt
) {}
