package com.example.likelionbackend2team.domain.sanction;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.example.likelionbackend2team.domain.user.User;
import com.example.likelionbackend2team.domain.admin.Admin;

@Entity
@Table(name = "user_sanctions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSanction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sanctionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}