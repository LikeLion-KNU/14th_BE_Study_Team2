package com.example.community.domain.admin.dto.response;

import java.time.LocalDateTime;

/**
 * 회원 상세 조회 응답 DTO
 *
 * postCount, commentCount:
 * → DB에 컬럼으로 저장된 값이 아님
 * → Service에서 PostRepository, CommentRepository로 별도 COUNT 쿼리 실행 후 전달
 * → 엔티티에 없는 계산된 값도 DTO에서 자유롭게 조합 가능
 *
 * status, role은 String으로 반환:
 * → UserStatus.APPROVED → "APPROVED" (enum.name())
 * → 프론트엔드가 enum 타입을 몰라도 문자열로 처리 가능
 */
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
