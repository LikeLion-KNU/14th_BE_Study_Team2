package com.example.community.domain.admin.dto.response;

import com.example.community.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PendingUserResponse {
    private Long userId;
    private Long studentId;
    private String name;
    private String school;
    private String certificateUrl;
    private LocalDateTime createdAt;

    public static PendingUserResponse from(User user) {
        return new PendingUserResponse(
            user.getId(), user.getStudentId(), user.getName(),
            user.getSchool(), user.getCertificateUrl(), user.getCreatedAt()
        );
    }
}
