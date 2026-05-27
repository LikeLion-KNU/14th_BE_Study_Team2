package com.example.community.auth;

import com.example.community.domain.user.entity.NicknameAdjective;
import com.example.community.domain.user.entity.NicknameNoun;
import com.example.community.domain.user.repository.NicknameAdjectiveRepository;
import com.example.community.domain.user.repository.NicknameNounRepository;
import com.example.community.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerSignupTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserRepository userRepository;

    @Autowired
    NicknameAdjectiveRepository adjectiveRepository;

    @Autowired
    NicknameNounRepository nounRepository;

    @Test
    @DisplayName("회원가입 API가 성공하면 201과 회원가입 응답을 반환한다")
    void signupApiSuccess() throws Exception {
        adjectiveRepository.save(NicknameAdjective.create("차분한"));
        nounRepository.save(NicknameNoun.create("고양이"));

        String requestBody = """
                {
                  "studentId": 20222001,
                  "password": "Password123",
                  "name": "컨트롤러회원가입",
                  "school": "컴퓨터학부",
                  "certificateUrl": "https://example.com/cert.png"
                }
                """;

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.studentId").value(20222001))
                .andExpect(jsonPath("$.nickname").value("차분한고양이"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        assertThat(userRepository.existsByStudentId(20222001L)).isTrue();
    }

    @Test
    @DisplayName("회원가입 API에서 필수값이 비어 있으면 400을 반환한다")
    void signupApiFailByBlankName() throws Exception {
        adjectiveRepository.save(NicknameAdjective.create("용감한"));
        nounRepository.save(NicknameNoun.create("사자"));

        String requestBody = """
                {
                  "studentId": 20222002,
                  "password": "Password123",
                  "name": "",
                  "school": "컴퓨터학부",
                  "certificateUrl": "https://example.com/cert.png"
                }
                """;

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("name 값이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("회원가입 API에서 비밀번호 형식이 맞지 않으면 400을 반환한다")
    void signupApiFailByInvalidPassword() throws Exception {
        adjectiveRepository.save(NicknameAdjective.create("빠른"));
        nounRepository.save(NicknameNoun.create("호랑이"));

        String requestBody = """
                {
                  "studentId": 20222003,
                  "password": "1234",
                  "name": "비밀번호오류",
                  "school": "컴퓨터학부",
                  "certificateUrl": "https://example.com/cert.png"
                }
                """;

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("비밀번호 형식 오류 (영문 포함, 숫자 포함, 8자 이상 20자 이하"));
    }

    @Test
    @DisplayName("회원가입 API에서 중복 학번이면 400을 반환한다")
    void signupApiFailByDuplicatedStudentId() throws Exception {
        adjectiveRepository.save(NicknameAdjective.create("즐거운"));
        nounRepository.save(NicknameNoun.create("독수리"));

        String requestBody = """
                {
                  "studentId": 20222004,
                  "password": "Password123",
                  "name": "첫번째유저",
                  "school": "컴퓨터학부",
                  "certificateUrl": "https://example.com/cert1.png"
                }
                """;

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        String duplicatedRequestBody = """
                {
                  "studentId": 20222004,
                  "password": "Password123",
                  "name": "두번째유저",
                  "school": "컴퓨터학부",
                  "certificateUrl": "https://example.com/cert2.png"
                }
                """;

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicatedRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("중복 학번입니다."));
    }
}