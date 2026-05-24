package com.example.community.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record WithdrawRequest(
    @NotBlank(message = "탈퇴 사유를 입력해주세요.") String reason
) {}
