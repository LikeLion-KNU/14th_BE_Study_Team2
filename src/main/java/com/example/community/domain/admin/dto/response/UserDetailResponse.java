package com.example.community.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserDetailResponse {
    private Long userId;
    private Long studentId;
    private String nickname;
    private String name;
    private String school;
    private String status;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private Long postCount;
    private Long commentCount;
}
