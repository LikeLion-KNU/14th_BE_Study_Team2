package com.example.community.auth;

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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerLoginTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

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
    @DisplayName("APPROVED 회원은 로그인 API 성공 시 JWT를 응답받는다")
    void loginApiSuccess() throws Exception {
        adjectiveRepository.save(NicknameAdjective.create("차분한"));
        nounRepository.save(NicknameNoun.create("고양이"));

        authService.signup(new SignupRequest(
                20224001L,
                "Password123",
                "로그인API유저",
                "컴퓨터학부",
                "https://example.com/cert.png"
        ));

        User user = userRepository.findByStudentId(20224001L)
                .orElseThrow();

        user.approve(LocalDateTime.now());

        String loginRequestBody = """
                {
                  "studentId": 20224001,
                  "password": "Password123"
                }
                """;

        String responseBody = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(jwtTokenProvider.getAccessTokenValiditySeconds()))
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.studentId").value(20224001))
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String accessToken = jsonNode.get("accessToken").asText();

        assertThat(jwtTokenProvider.validateToken(accessToken)).isTrue();

        String sessionId = jwtTokenProvider.getSessionId(accessToken);

        assertThat(activeSessionRepository.existsBySessionIdAndExpiresAtAfter(
                sessionId,
                LocalDateTime.now()
        )).isTrue();
    }

    @Test
    @DisplayName("PENDING 회원은 로그인 API에서 403을 반환한다")
    void loginApiFailByPendingUser() throws Exception {
        adjectiveRepository.save(NicknameAdjective.create("빠른"));
        nounRepository.save(NicknameNoun.create("호랑이"));

        authService.signup(new SignupRequest(
                20224002L,
                "Password123",
                "승인대기API유저",
                "컴퓨터학부",
                "https://example.com/cert.png"
        ));

        String loginRequestBody = """
                {
                  "studentId": 20224002,
                  "password": "Password123"
                }
                """;

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("반려/정지된 회원"))
                .andExpect(jsonPath("$.path").value("/api/users/login"));
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인 API에서 401을 반환한다")
    void loginApiFailByWrongPassword() throws Exception {
        adjectiveRepository.save(NicknameAdjective.create("용감한"));
        nounRepository.save(NicknameNoun.create("사자"));

        authService.signup(new SignupRequest(
                20224003L,
                "Password123",
                "비번오류API유저",
                "컴퓨터학부",
                "https://example.com/cert.png"
        ));

        User user = userRepository.findByStudentId(20224003L)
                .orElseThrow();

        user.approve(LocalDateTime.now());

        String loginRequestBody = """
                {
                  "studentId": 20224003,
                  "password": "WrongPassword123"
                }
                """;

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("정보가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 학번이면 로그인 API에서 401을 반환한다")
    void loginApiFailByUnknownStudentId() throws Exception {
        String loginRequestBody = """
                {
                  "studentId": 99999999,
                  "password": "Password123"
                }
                """;

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("정보가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("로그인 API 요청값이 비어 있으면 400을 반환한다")
    void loginApiFailByInvalidRequest() throws Exception {
        String loginRequestBody = """
                {
                  "studentId": 20224004,
                  "password": ""
                }
                """;

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("password 값이 올바르지 않습니다."));
    }
}