package com.example.community.api.user.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record AuthErrorResponse(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp
) {

    public static AuthErrorResponse of(HttpStatus status, String message, String path) {
        return new AuthErrorResponse(
                status.value(),
                status.name(),
                message,
                path,
                LocalDateTime.now()
        );
    }
}