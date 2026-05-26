package com.example.community.domain.admin.dto.response;

import java.time.LocalDateTime;

/**
 * 가입 승인 응답 DTO
 *
 * approvedAt: 승인 시각
 * → Service에서 user.approve() 호출 후 user.getApprovedAt()으로 가져옴
 * → approve() 내부에서 LocalDateTime.now()로 설정됨 (클라이언트가 조작 불가)
 */
public record ApproveResponse(
    Long userId,
    String status,
    LocalDateTime approvedAt
) {}
