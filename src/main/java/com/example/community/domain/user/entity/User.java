package com.example.community.domain.user.entity;

import com.example.community.domain.common.BaseTimeEntity;
import com.example.community.domain.user.enums.UserRole;
import com.example.community.domain.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "student_id", unique = true, nullable = false)
    private Long studentId;

    @Column(name = "hashed_pw", nullable = false)
    private String hashedPw;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private String school;

    @Column(unique = true, nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Column(name = "certificate_url", nullable = false)
    private String certificateUrl;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}