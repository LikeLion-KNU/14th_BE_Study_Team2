package com.example.community.security.principal;

import com.example.community.domain.user.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomUserPrincipal {

    private final Long userId;
    private final Long studentId;
    private final UserRole role;
    private final String sessionId;
}