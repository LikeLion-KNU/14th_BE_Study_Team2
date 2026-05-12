package com.example.community.api.user.dto;

import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.enums.UserStatus;

public record SignupResponse(
        Long userId,
        Long studentId,
        String nickname,
        UserStatus status
) {

    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(),
                user.getStudentId(),
                user.getNickname(),
                user.getStatus()
        );
    }
}