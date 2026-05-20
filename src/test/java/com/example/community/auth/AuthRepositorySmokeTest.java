package com.example.community.auth;

import com.example.community.domain.user.entity.ActiveSession;
import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.enums.UserRole;
import com.example.community.domain.user.enums.UserStatus;
import com.example.community.domain.user.repository.ActiveSessionRepository;
import com.example.community.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AuthRepositorySmokeTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ActiveSessionRepository activeSessionRepository;

    @Test
    @DisplayName("User 저장, 조회, 중복 확인이 동작한다")
    void userRepositoryWorks() {
        User user = User.createPendingUser(
                20220001L,
                "hashed-password",
                "테스트유저",
                "컴퓨터학부",
                "빠른고양이",
                "https://example.com/cert.png"
        );

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);

        assertThat(userRepository.existsByStudentId(20220001L)).isTrue();
        assertThat(userRepository.existsByNickname("빠른고양이")).isTrue();

        User foundUser = userRepository.findByStudentId(20220001L)
                .orElseThrow();

        assertThat(foundUser.getStudentId()).isEqualTo(20220001L);
        assertThat(foundUser.getName()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("ActiveSession 저장, 조회, 삭제가 동작한다")
    void activeSessionRepositoryWorks() {
        User user = User.createPendingUser(
                20220002L,
                "hashed-password",
                "세션유저",
                "컴퓨터학부",
                "용감한사자",
                "https://example.com/cert.png"
        );

        User savedUser = userRepository.save(user);

        ActiveSession activeSession = ActiveSession.create(
                "test-session-id",
                savedUser,
                "127.0.0.1",
                LocalDateTime.now().plusHours(1)
        );

        activeSessionRepository.save(activeSession);

        assertThat(activeSessionRepository.existsBySessionIdAndExpiresAtAfter(
                "test-session-id",
                LocalDateTime.now()
        )).isTrue();

        activeSessionRepository.deleteBySessionId("test-session-id");

        assertThat(activeSessionRepository.existsBySessionIdAndExpiresAtAfter(
                "test-session-id",
                LocalDateTime.now()
        )).isFalse();
    }
}