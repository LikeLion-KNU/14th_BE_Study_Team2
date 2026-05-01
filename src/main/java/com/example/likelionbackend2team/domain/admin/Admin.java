package com.example.likelionbackend2team.domain.admin;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.example.likelionbackend2team.domain.user.User;

@Entity
@Table(name = "admins")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adminId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 50)
    private String department;

    private Boolean isActive;

    @Column(nullable = false)
    private LocalDateTime grantedAt;

    private LocalDateTime lastLoginAt;
}