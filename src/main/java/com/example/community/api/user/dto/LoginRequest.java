package com.example.community.api.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(

        @NotNull
        Long studentId,

        @NotBlank
        String password
) {
}