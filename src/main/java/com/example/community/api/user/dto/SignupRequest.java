package com.example.community.api.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SignupRequest(

        @NotNull
        Long studentId,

        @NotBlank
        String password,

        @NotBlank
        String name,

        @NotBlank
        String school,

        @NotBlank
        String certificateUrl
) {
}