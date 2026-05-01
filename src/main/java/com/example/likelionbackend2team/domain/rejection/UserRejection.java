package com.example.likelionbackend2team.domain.rejection;

import com.example.likelionbackend2team.domain.admin.Admin;
import com.example.likelionbackend2team.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_rejections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRejection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rejectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    @Column(nullable = false, length = 255)
    private String reason;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
