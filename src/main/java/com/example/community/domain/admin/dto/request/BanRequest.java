package com.example.community.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BanRequest(
    @NotBlank(message = "정지 사유를 입력해주세요.") String reason
) {}
