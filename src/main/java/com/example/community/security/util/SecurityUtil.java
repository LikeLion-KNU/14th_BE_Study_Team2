package com.example.community.security.util;

import com.example.community.api.user.exception.AuthException;
import com.example.community.domain.user.enums.UserRole;
import com.example.community.security.principal.CustomUserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static CustomUserPrincipal getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof CustomUserPrincipal principal)) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }

        return principal;
    }

    public static Long getCurrentUserId() {
        return getCurrentUserPrincipal().getUserId();
    }

    public static Long getCurrentStudentId() {
        return getCurrentUserPrincipal().getStudentId();
    }

    public static UserRole getCurrentUserRole() {
        return getCurrentUserPrincipal().getRole();
    }

    public static String getCurrentSessionId() {
        return getCurrentUserPrincipal().getSessionId();
    }
}