package com.example.community.auth;

import com.example.community.api.user.dto.LoginRequest;
import com.example.community.api.user.dto.LoginResponse;
import com.example.community.api.user.dto.SignupRequest;
import com.example.community.api.user.exception.AuthException;
import com.example.community.api.user.service.AuthService;
import com.example.community.domain.user.entity.NicknameAdjective;
import com.example.community.domain.user.entity.NicknameNoun;
import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.repository.ActiveSessionRepository;
import com.example.community.domain.user.repository.NicknameAdjectiveRepository;
import com.example.community.domain.user.repository.NicknameNounRepository;
import com.example.community.domain.user.repository.UserRepository;
import com.example.community.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AuthServiceLoginTest {

    @Autowired
    AuthService authService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ActiveSessionRepository activeSessionRepository;

    @Autowired
    NicknameAdjectiveRepository adjectiveRepository;

    @Autowired
    NicknameNounRepository nounRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("APPROVED 회원은 로그인 성공 시 JWT와 ActiveSession을 발급받는다")
    void loginSuccess() {
        adjectiveRepository.save(NicknameAdjective.create("차분한"));
        nounRepository.save(NicknameNoun.create("고양이"));

        authService.signup(new SignupRequest(
                20223001L,
                "Password123",
                "로그인성공유저",
                "컴퓨터학부",
                "https://example.com/cert.png"
        ));

        User user = userRepository.findByStudentId(20223001L)
                .orElseThrow();

        user.approve(LocalDateTime.now());

        LoginResponse response = authService.login(
                new LoginRequest(20223001L, "Password123"),
                "127.0.0.1"
        );

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(jwtTokenProvider.getAccessTokenValiditySeconds());
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.studentId()).isEqualTo(20223001L);
        assertThat(response.role().name()).isEqualTo("USER");

        assertThat(jwtTokenProvider.validateToken(response.accessToken())).isTrue();
        assertThat(jwtTokenProvider.getUserId(response.accessToken())).isEqualTo(user.getId());
        assertThat(jwtTokenProvider.getRole(response.accessToken())).isEqualTo("USER");

        String sessionId = jwtTokenProvider.getSessionId(response.accessToken());

        assertThat(activeSessionRepository.existsBySessionIdAndExpiresAtAfter(
                sessionId,
                LocalDateTime.now()
        )).isTrue();
    }

    @Test
    @DisplayName("PENDING 회원은 로그인 시 403으로 차단된다")
    void loginFailByPendingUser() {
        adjectiveRepository.save(NicknameAdjective.create("빠른"));
        nounRepository.save(NicknameNoun.create("호랑이"));

        authService.signup(new SignupRequest(
                20223002L,
                "Password123",
                "승인대기유저",
                "컴퓨터학부",
                "https://example.com/cert.png"
        ));

        assertThatThrownBy(() -> authService.login(
                new LoginRequest(20223002L, "Password123"),
                "127.0.0.1"
        ))
                .isInstanceOf(AuthException.class)
                .hasMessage("반려/정지된 회원");
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인 시 401로 실패한다")
    void loginFailByWrongPassword() {
        adjectiveRepository.save(NicknameAdjective.create("용감한"));
        nounRepository.save(NicknameNoun.create("사자"));

        authService.signup(new SignupRequest(
                20223003L,
                "Password123",
                "비번오류유저",
                "컴퓨터학부",
                "https://example.com/cert.png"
        ));

        User user = userRepository.findByStudentId(20223003L)
                .orElseThrow();

        user.approve(LocalDateTime.now());

        assertThatThrownBy(() -> authService.login(
                new LoginRequest(20223003L, "WrongPassword123"),
                "127.0.0.1"
        ))
                .isInstanceOf(AuthException.class)
                .hasMessage("정보가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 학번이면 로그인 시 401로 실패한다")
    void loginFailByUnknownStudentId() {
        assertThatThrownBy(() -> authService.login(
                new LoginRequest(99999999L, "Password123"),
                "127.0.0.1"
        ))
                .isInstanceOf(AuthException.class)
                .hasMessage("정보가 일치하지 않습니다.");
    }
}