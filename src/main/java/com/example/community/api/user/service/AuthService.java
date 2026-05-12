package com.example.community.api.user.service;

import com.example.community.api.user.dto.LoginRequest;
import com.example.community.api.user.dto.LoginResponse;
import com.example.community.api.user.dto.SignupRequest;
import com.example.community.api.user.dto.SignupResponse;
import com.example.community.api.user.exception.AuthException;
import com.example.community.domain.user.entity.ActiveSession;
import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.repository.ActiveSessionRepository;
import com.example.community.domain.user.repository.UserRepository;
import com.example.community.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,20}$");

    private final UserRepository userRepository;
    private final ActiveSessionRepository activeSessionRepository;
    private final NicknameService nicknameService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public SignupResponse signup(SignupRequest request) {
        validatePassword(request.password());
        validateDuplicatedStudentId(request.studentId());

        String nickname = nicknameService.generateUniqueNickname();
        String hashedPassword = passwordEncoder.encode(request.password());

        User user = User.createPendingUser(
                request.studentId(),
                hashedPassword,
                request.name(),
                request.school(),
                nickname,
                request.certificateUrl()
        );

        User savedUser = userRepository.save(user);

        return SignupResponse.from(savedUser);
    }

    public LoginResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByStudentId(request.studentId())
                .orElseThrow(() -> new AuthException(
                        HttpStatus.UNAUTHORIZED,
                        "정보가 일치하지 않습니다."
                ));

        if (!passwordEncoder.matches(request.password(), user.getHashedPw())) {
            throw new AuthException(
                    HttpStatus.UNAUTHORIZED,
                    "정보가 일치하지 않습니다."
            );
        }

        if (!user.isApproved()) {
            throw new AuthException(
                    HttpStatus.FORBIDDEN,
                    "반려/정지된 회원"
            );
        }

        String sessionId = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getAccessTokenValiditySeconds());

        ActiveSession activeSession = ActiveSession.create(
                sessionId,
                user,
                ipAddress,
                expiresAt
        );

        activeSessionRepository.save(activeSession);

        String accessToken = jwtTokenProvider.createAccessToken(
                user.getId(),
                user.getStudentId(),
                user.getRole(),
                sessionId
        );

        return LoginResponse.of(
                accessToken,
                jwtTokenProvider.getAccessTokenValiditySeconds(),
                user
        );
    }

    private void validatePassword(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new AuthException(
                    HttpStatus.BAD_REQUEST,
                    "비밀번호 형식 오류"
            );
        }
    }

    private void validateDuplicatedStudentId(Long studentId) {
        if (userRepository.existsByStudentId(studentId)) {
            throw new AuthException(
                    HttpStatus.BAD_REQUEST,
                    "중복 학번입니다."
            );
        }
    }
}