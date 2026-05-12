package com.example.community.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "active_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ActiveSession {

    @Id // 이 테이블은 DBML상 pk가 varchar 타입의 session_id 입니다.
    @Column(name = "session_id")
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public static ActiveSession create(
            String sessionId,
            User user,
            String ipAddress,
            LocalDateTime expiresAt
    ) {
        ActiveSession activeSession = new ActiveSession();
        activeSession.sessionId = sessionId;
        activeSession.user = user;
        activeSession.ipAddress = ipAddress;
        activeSession.expiresAt = expiresAt;
        return activeSession;
    }

    public boolean isExpired(LocalDateTime now) {
        return this.expiresAt.isBefore(now);
    }
}