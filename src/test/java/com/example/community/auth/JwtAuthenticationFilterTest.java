package com.example.community.auth;

import com.example.community.api.user.dto.LoginRequest;
import com.example.community.api.user.dto.SignupRequest;
import com.example.community.api.user.service.AuthService;
import com.example.community.domain.user.entity.NicknameAdjective;
import com.example.community.domain.user.entity.NicknameNoun;
import com.example.community.domain.user.entity.User;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class JwtAuthenticationFilterTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AuthService authService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    NicknameAdjectiveRepository adjectiveRepository;

    @Autowired
    NicknameNounRepository nounRepository;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("토큰 없이 보호 API에 접근하면 401을 반환한다")
    void protectedApiFailWithoutToken() throws Exception {
        mockMvc.perform(get("/api/test/jwt-auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                .andExpect(jsonPath("$.path").value("/api/test/jwt-auth"));
    }

    @Test
    @DisplayName("유효한 JWT로 보호 API에 접근하면 현재 사용자 정보를 조회할 수 있다")
    void protectedApiSuccessWithValidToken() throws Exception {
        String accessToken = createApprovedUserAndLogin(
                20225001L,
                "차분한",
                "고양이"
        );

        User user = userRepository.findByStudentId(20225001L)
                .orElseThrow();

        String sessionId = jwtTokenProvider.getSessionId(accessToken);

        mockMvc.perform(get("/api/test/jwt-auth")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.studentId").value(20225001))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.sessionId").value(sessionId));
    }

    @Test
    @DisplayName("JWT는 유효하지만 ActiveSession이 없으면 401을 반환한다")
    void protectedApiFailByMissingActiveSession() throws Exception {
        adjectiveRepository.save(NicknameAdjective.create("빠른"));
        nounRepository.save(NicknameNoun.create("호랑이"));

        authService.signup(new SignupRequest(
                20225002L,
                "Password123",
                "세션없는유저",
                "컴퓨터학부",
                "https://example.com/cert.png"
        ));

        User user = userRepository.findByStudentId(20225002L)
                .orElseThrow();

        user.approve(LocalDateTime.now());

        String fakeSessionId = UUID.randomUUID().toString();

        String tokenWithoutActiveSession = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getStudentId(),
                user.getRole(),
                fakeSessionId
        );

        mockMvc.perform(get("/api/test/jwt-auth")
                        .header("Authorization", "Bearer " + tokenWithoutActiveSession))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    @DisplayName("USER 권한으로 관리자 API에 접근하면 403을 반환한다")
    void adminApiFailByUserRole() throws Exception {
        String accessToken = createApprovedUserAndLogin(
                20225003L,
                "용감한",
                "사자"
        );

        mockMvc.perform(get("/api/admin/jwt-test")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."))
                .andExpect(jsonPath("$.path").value("/api/admin/jwt-test"));
    }

    private String createApprovedUserAndLogin(Long studentId, String adjective, String noun) {
        adjectiveRepository.save(NicknameAdjective.create(adjective));
        nounRepository.save(NicknameNoun.create(noun));

        authService.signup(new SignupRequest(
                studentId,
                "Password123",
                "JWT테스트유저",
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
    static class JwtTestControllerConfig {

        @Bean
        JwtTestController jwtTestController() {
            return new JwtTestController();
        }
    }

    @RestController
    static class JwtTestController {

        @GetMapping("/api/test/jwt-auth")
        public Map<String, Object> jwtAuthTest() {
            return Map.of(
                    "userId", SecurityUtil.getCurrentUserId(),
                    "studentId", SecurityUtil.getCurrentStudentId(),
                    "role", SecurityUtil.getCurrentUserRole().name(),
                    "sessionId", SecurityUtil.getCurrentSessionId()
            );
        }

        @GetMapping("/api/admin/jwt-test")
        public Map<String, Object> adminJwtTest() {
            return Map.of("message", "admin ok");
        }
    }
}