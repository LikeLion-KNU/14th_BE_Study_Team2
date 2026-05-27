package com.example.community.domain.admin.dto.response;

import java.time.LocalDateTime;

/**
 * 회원 정지 응답 DTO
 *
 * startAt, endAt을 응답에 포함하는 이유:
 * → 프론트엔드가 "언제까지 정지인지" 사용자에게 표시할 수 있도록
 * → endAt = startAt + 7일 (서버에서 고정 계산, 클라이언트 조작 불가)
 */
public record BanResponse(
    Long userId,
    String status,
    LocalDateTime startAt,
    LocalDateTime endAt
) {}
