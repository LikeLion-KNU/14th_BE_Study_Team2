package com.example.community.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BanRequest {
    @NotBlank(message = "정지 사유를 입력해주세요.")
    private String reason;
}
