package com.example.community.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 회원 정지 요청 DTO
 * RejectRequest와 동일한 record 구조 — reason 필드만 받음
 *
 * 같은 구조지만 별도 클래스로 분리한 이유:
 * → 추후 BanRequest에만 기간(days) 같은 필드가 추가될 수 있음
 * → 각 API의 요청/응답 구조가 독립적으로 변경될 수 있도록 분리 유지 (YAGNI 원칙)
 */
public record BanRequest(
    @NotBlank(message = "정지 사유를 입력해주세요.") String reason
) {}
