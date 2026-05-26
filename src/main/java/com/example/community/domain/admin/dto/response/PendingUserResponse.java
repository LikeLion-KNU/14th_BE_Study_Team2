package com.example.community.domain.admin.dto.response;

import com.example.community.domain.user.entity.User;

import java.time.LocalDateTime;

/**
 * 가입 대기 회원 목록 응답 DTO
 *
 * [정적 팩토리 메서드 패턴: from(User)]
 * → Entity를 DTO로 변환하는 책임을 DTO 자체가 가짐
 * → Service에서 new PendingUserResponse(user.getId(), ...) 대신 PendingUserResponse.from(user) 한 줄로 처리
 * → 변환 로직이 한 곳에 모여 있어 필드 추가 시 수정 지점이 하나
 *
 * Service에서의 사용:
 *   userRepository.findByStatus(PENDING, pageable).map(PendingUserResponse::from)
 *   → :: 는 메서드 참조. user -> PendingUserResponse.from(user) 와 동일
 */
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
