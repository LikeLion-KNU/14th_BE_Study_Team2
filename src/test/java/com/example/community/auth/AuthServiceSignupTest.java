package com.example.community.auth;

import com.example.community.api.user.dto.SignupRequest;
import com.example.community.api.user.dto.SignupResponse;
import com.example.community.api.user.exception.AuthException;
import com.example.community.api.user.service.AuthService;
import com.example.community.domain.user.entity.NicknameAdjective;
import com.example.community.domain.user.entity.NicknameNoun;
import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.enums.UserRole;
import com.example.community.domain.user.enums.UserStatus;
import com.example.community.domain.user.repository.NicknameAdjectiveRepository;
import com.example.community.domain.user.repository.NicknameNounRepository;
import com.example.community.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class AuthServiceSignupTest {

    @Autowired
    AuthService authService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    NicknameAdjectiveRepository adjectiveRepository;

    @Autowired
    NicknameNounRepository nounRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 성공 시 PENDING USER가 저장되고 비밀번호는 BCrypt로 암호화된다")
    void signupSuccess() {
        adjectiveRepository.save(NicknameAdjective.create("차분한"));
        nounRepository.save(NicknameNoun.create("고양이"));

        SignupRequest request = new SignupRequest(
                20221001L,
                "Password123",
                "회원가입유저",
                "컴퓨터학부",
                "https://example.com/cert.png"
        );

        SignupResponse response = authService.signup(request);

        assertThat(response.userId()).isNotNull();
        assertThat(response.studentId()).isEqualTo(20221001L);
        assertThat(response.status()).isEqualTo(UserStatus.PENDING);
        assertThat(response.nickname()).isEqualTo("차분한고양이");

        User savedUser = userRepository.findByStudentId(20221001L)
                .orElseThrow();

        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
        assertThat(savedUser.getHashedPw()).isNotEqualTo("Password123");
        assertThat(passwordEncoder.matches("Password123", savedUser.getHashedPw())).isTrue();
    }

    @Test
    @DisplayName("중복 학번이면 회원가입에 실패한다")
    void signupFailByDuplicatedStudentId() {
        adjectiveRepository.save(NicknameAdjective.create("빠른"));
        nounRepository.save(NicknameNoun.create("호랑이"));

        SignupRequest request = new SignupRequest(
                20221002L,
                "Password123",
                "첫번째유저",
                "컴퓨터학부",
                "https://example.com/cert1.png"
        );

        authService.signup(request);

        SignupRequest duplicatedRequest = new SignupRequest(
                20221002L,
                "Password123",
                "두번째유저",
                "컴퓨터학부",
                "https://example.com/cert2.png"
        );

        assertThatThrownBy(() -> authService.signup(duplicatedRequest))
                .isInstanceOf(AuthException.class)
                .hasMessage("중복 학번입니다.");
    }

    @Test
    @DisplayName("비밀번호 형식이 맞지 않으면 회원가입에 실패한다")
    void signupFailByInvalidPassword() {
        adjectiveRepository.save(NicknameAdjective.create("즐거운"));
        nounRepository.save(NicknameNoun.create("사자"));

        SignupRequest request = new SignupRequest(
                20221003L,
                "1234",
                "비밀번호오류유저",
                "컴퓨터학부",
                "https://example.com/cert.png"
        );

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(AuthException.class)
                .hasMessage("비밀번호 형식 오류 (영문 포함, 숫자 포함, 8자 이상 20자 이하");

        assertThat(userRepository.existsByStudentId(20221003L)).isFalse();
    }

    @Test
    @DisplayName("닉네임 단어 데이터가 없으면 회원가입에 실패한다")
    void signupFailByEmptyNicknameWords() {
        SignupRequest request = new SignupRequest(
                20221004L,
                "Password123",
                "닉네임오류유저",
                "컴퓨터학부",
                "https://example.com/cert.png"
        );

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(AuthException.class)
                .hasMessage("닉네임 단어 데이터가 부족합니다.");
    }
}