package com.example.community.api.user.dto;

public record LogoutResponse(
        String message
) {

    public static LogoutResponse success() {
        return new LogoutResponse("로그아웃 성공");
    }
}