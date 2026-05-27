package com.example.community.domain.admin.dto.response;

/**
 * 가입 반려 응답 DTO
 *
 * reason은 응답에 포함하지 않음 — user_sanctions 테이블에 이력으로 저장됨
 * 반려 후 상태(REJECTED)만 확인하면 충분
 */
public record RejectResponse(
    Long userId,
    String status
) {}
