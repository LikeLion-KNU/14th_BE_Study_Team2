package com.example.community.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BanResponse {
    private Long userId;
    private String status;
    private LocalDateTime startAt;
    private LocalDateTime endAt; // startAt + 7일 고정
}
