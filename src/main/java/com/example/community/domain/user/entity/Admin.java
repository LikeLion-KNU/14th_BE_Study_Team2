package com.example.community.domain.user.entity;

import com.example.community.domain.user.enums.AdminLevel;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admins")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_level", nullable = false)
    private AdminLevel adminLevel = AdminLevel.STAFF;

    @Column(length = 50)
    private String department;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "granted_at", nullable = false)
    private LocalDateTime grantedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    public static Admin create(User user, AdminLevel adminLevel) {
        Admin admin = new Admin();
        admin.user = user;
        admin.adminLevel = adminLevel;
        admin.grantedAt = LocalDateTime.now();
        admin.isActive = true;
        return admin;
    }
}