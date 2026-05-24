package com.example.community.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApproveResponse {
    private Long userId;
    private String status;
    private LocalDateTime approvedAt;
}
