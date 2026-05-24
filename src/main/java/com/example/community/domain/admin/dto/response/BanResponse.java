package com.example.community.domain.admin.dto.response;

import java.time.LocalDateTime;

public record BanResponse(
    Long userId,
    String status,
    LocalDateTime startAt,
    LocalDateTime endAt
) {}
