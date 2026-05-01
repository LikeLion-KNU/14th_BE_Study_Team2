
package com.example.likelionbackend2team.domain.session;

import com.example.likelionbackend2team.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "active_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActiveSession {

    @Id
    @Column(length = 255)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 45)
    private String ipAddress;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}