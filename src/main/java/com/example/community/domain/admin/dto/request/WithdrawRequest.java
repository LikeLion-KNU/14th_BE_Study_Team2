package com.example.community.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 강제 탈퇴 요청 DTO
 * reason은 user_sanctions 테이블에 FORCE_WITHDRAW 이력으로 저장됨
 */
public record WithdrawRequest(
    @NotBlank(message = "탈퇴 사유를 입력해주세요.") String reason
) {}
