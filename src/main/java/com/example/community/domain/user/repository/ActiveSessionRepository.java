package com.example.community.domain.user.repository;

import com.example.community.domain.user.entity.ActiveSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ActiveSessionRepository extends JpaRepository<ActiveSession, String> {

    boolean existsBySessionIdAndExpiresAtAfter(String sessionId, LocalDateTime now);

    void deleteBySessionId(String sessionId);
}