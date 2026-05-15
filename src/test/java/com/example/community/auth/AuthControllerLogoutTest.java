package com.example.community.auth;

import com.example.community.api.user.dto.LoginRequest;
import com.example.community.api.user.dto.SignupRequest;
import com.example.community.api.user.service.AuthService;
import com.example.community.domain.user.entity.NicknameAdjective;
import com.example.community.domain.user.entity.NicknameNoun;
import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.repository.ActiveSessionRepository;
import com.example.community.domain.user.repository.NicknameAdjectiveRepository;
import com.example.community.domain.user.repository.NicknameNounRepository;
import com.example.community.domain.user.repository.UserRepository;
import com.example.community.security.jwt.JwtTokenProvider;
import com.example.community.security.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerLogoutTest {

    @Autowired
    MockMvc mockMvc;

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
    @DisplayName("로그아웃 성공 시 ActiveSession이 삭제된다")
    void logoutSuccess() throws Exception {
        String accessToken = createApprovedUserAndLogin(
                20226001L,
                "차분한",
                "고양이"
        );

        String sessionId = jwtTokenProvider.getSessionId(accessToken);

        assertThat(activeSessionRepository.existsBySessionIdAndExpiresAtAfter(
                sessionId,
                LocalDateTime.now()
        )).isTrue();

        mockMvc.perform(post("/api/users/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃 성공"));

        assertThat(activeSessionRepository.existsBySessionIdAndExpiresAtAfter(
                sessionId,
                LocalDateTime.now()
        )).isFalse();
    }

    @Test
    @DisplayName("로그아웃 후 같은 JWT로 보호 API에 접근하면 401을 반환한다")
    void oldTokenFailAfterLogout() throws Exception {
        String accessToken = createApprovedUserAndLogin(
                20226002L,
                "빠른",
                "호랑이"
        );

        mockMvc.perform(post("/api/users/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/test/logout-auth")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    @DisplayName("토큰 없이 로그아웃 API를 호출하면 401을 반환한다")
    void logoutFailWithoutToken() throws Exception {
        mockMvc.perform(post("/api/users/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    private String createApprovedUserAndLogin(Long studentId, String adjective, String noun) {
        adjectiveRepository.save(NicknameAdjective.create(adjective));
        nounRepository.save(NicknameNoun.create(noun));

        authService.signup(new SignupRequest(
                studentId,
                "Password123",
                "로그아웃테스트유저",
                "컴퓨터학부",
                "https://example.com/cert.png"
        ));

        User user = userRepository.findByStudentId(studentId)
                .orElseThrow();

        user.approve(LocalDateTime.now());

        return authService.login(
                new LoginRequest(studentId, "Password123"),
                "127.0.0.1"
        ).accessToken();
    }

    @TestConfiguration
    static class LogoutTestControllerConfig {

        @Bean
        LogoutTestController logoutTestController() {
            return new LogoutTestController();
        }
    }

    @RestController
    static class LogoutTestController {

        @GetMapping("/api/test/logout-auth")
        public Map<String, Object> logoutAuthTest() {
            return Map.of(
                    "userId", SecurityUtil.getCurrentUserId(),
                    "studentId", SecurityUtil.getCurrentStudentId(),
                    "role", SecurityUtil.getCurrentUserRole().name(),
                    "sessionId", SecurityUtil.getCurrentSessionId()
            );
        }
    }
}