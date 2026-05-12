package com.example.community.auth;

import com.example.community.domain.user.entity.NicknameAdjective;
import com.example.community.domain.user.entity.NicknameNoun;
import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.repository.ActiveSessionRepository;
import com.example.community.domain.user.repository.NicknameAdjectiveRepository;
import com.example.community.domain.user.repository.NicknameNounRepository;
import com.example.community.domain.user.repository.UserRepository;
import com.example.community.security.jwt.JwtTokenProvider;
import com.example.community.security.util.SecurityUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthFullFlowIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

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
    @DisplayName("회원가입부터 로그인, JWT 인증, 로그아웃, 토큰 무효화까지 전체 인증 흐름이 동작한다")
    void fullAuthFlowSuccess() throws Exception {
        // given: 닉네임 단어 데이터 준비
        adjectiveRepository.save(NicknameAdjective.create("차분한"));
        nounRepository.save(NicknameNoun.create("고양이"));

        String signupRequestBody = """
                {
                  "studentId": 20239999,
                  "password": "Password123",
                  "name": "최종테스트",
                  "school": "컴퓨터학부",
                  "certificateUrl": "https://example.com/cert/20239999.png"
                }
                """;

        // 1. 회원가입 성공
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(signupRequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.studentId").value(20239999))
                .andExpect(jsonPath("$.status").value("PENDING"));

        User user = userRepository.findByStudentId(20239999L)
                .orElseThrow();

        assertThat(user.isApproved()).isFalse();
        assertThat(user.isLoginBlocked()).isTrue();

        // 2. 승인 전 로그인은 403
        String loginRequestBody = """
                {
                  "studentId": 20239999,
                  "password": "Password123"
                }
                """;

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("반려/정지된 회원"));

        // 3. 관리자 승인 상황을 테스트에서 직접 처리
        user.approve(LocalDateTime.now());

        assertThat(user.isApproved()).isTrue();

        // 4. 승인 후 로그인 성공 + JWT 발급
        String loginResponseBody = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(7200))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.studentId").value(20239999))
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponseBody);
        String accessToken = loginJson.get("accessToken").asText();

        assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();
        assertThat(jwtTokenProvider.getUserId(accessToken)).isEqualTo(user.getId());
        assertThat(jwtTokenProvider.getRole(accessToken)).isEqualTo("USER");

        String sessionId = jwtTokenProvider.getSessionId(accessToken);

        assertThat(activeSessionRepository.existsBySessionIdAndExpiresAtAfter(
                sessionId,
                LocalDateTime.now()
        )).isTrue();

        // 5. JWT로 보호 API 접근 성공
        mockMvc.perform(get("/api/test/final-auth")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.studentId").value(20239999))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.sessionId").value(sessionId));

        // 6. 일반 USER가 관리자 API 접근 시 403
        mockMvc.perform(get("/api/admin/final-test")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."));

        // 7. 로그아웃 성공
        mockMvc.perform(post("/api/users/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그아웃 성공"));

        assertThat(activeSessionRepository.existsBySessionIdAndExpiresAtAfter(
                sessionId,
                LocalDateTime.now()
        )).isFalse();

        // 8. 로그아웃 후 같은 JWT 재사용 실패
        mockMvc.perform(get("/api/test/final-auth")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    @DisplayName("토큰 없이 보호 API와 로그아웃 API에 접근하면 401을 반환한다")
    void protectedApisFailWithoutToken() throws Exception {
        mockMvc.perform(get("/api/test/final-auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));

        mockMvc.perform(post("/api/users/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @TestConfiguration
    static class FinalAuthTestControllerConfig {

        @Bean
        FinalAuthTestController finalAuthTestController() {
            return new FinalAuthTestController();
        }
    }

    @RestController
    static class FinalAuthTestController {

        @GetMapping("/api/test/final-auth")
        public Map<String, Object> finalAuthTest() {
            return Map.of(
                    "userId", SecurityUtil.getCurrentUserId(),
                    "studentId", SecurityUtil.getCurrentStudentId(),
                    "role", SecurityUtil.getCurrentUserRole().name(),
                    "sessionId", SecurityUtil.getCurrentSessionId()
            );
        }

        @GetMapping("/api/admin/final-test")
        public Map<String, Object> finalAdminTest() {
            return Map.of(
                    "message", "admin ok"
            );
        }
    }
}