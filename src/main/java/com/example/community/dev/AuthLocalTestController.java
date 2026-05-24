package com.example.community.dev;

import com.example.community.domain.user.entity.NicknameAdjective;
import com.example.community.domain.user.entity.NicknameNoun;
import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.repository.NicknameAdjectiveRepository;
import com.example.community.domain.user.repository.NicknameNounRepository;
import com.example.community.domain.user.repository.UserRepository;
import com.example.community.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Profile("local")
@RestController
@RequiredArgsConstructor
public class AuthLocalTestController {

    private final UserRepository userRepository;
    private final NicknameAdjectiveRepository adjectiveRepository;
    private final NicknameNounRepository nounRepository;

    @PostMapping("/api/auth-test/seed-nicknames")
    @Transactional
    public ResponseEntity<Map<String, Object>> seedNicknames() {
        saveAdjectiveIfNotExists("빠른");
        saveAdjectiveIfNotExists("차분한");
        saveAdjectiveIfNotExists("용감한");
        saveAdjectiveIfNotExists("즐거운");

        saveNounIfNotExists("호랑이");
        saveNounIfNotExists("고양이");
        saveNounIfNotExists("사자");
        saveNounIfNotExists("독수리");

        return ResponseEntity.ok(Map.of(
                "message", "닉네임 단어 세팅 완료"
        ));
    }

    @PostMapping("/api/auth-test/approve/{studentId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> approveUser(@PathVariable Long studentId) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 학번의 사용자가 없습니다."));

        user.approve(LocalDateTime.now());

        return ResponseEntity.ok(Map.of(
                "message", "임시 승인 완료",
                "userId", user.getId(),
                "studentId", user.getStudentId(),
                "status", user.getStatus().name(),
                "role", user.getRole().name(),
                "approvedAt", user.getApprovedAt().toString()
        ));
    }

    @GetMapping("/api/auth-test/me")
    public ResponseEntity<Map<String, Object>> me() {
        return ResponseEntity.ok(Map.of(
                "userId", SecurityUtil.getCurrentUserId(),
                "studentId", SecurityUtil.getCurrentStudentId(),
                "role", SecurityUtil.getCurrentUserRole().name(),
                "sessionId", SecurityUtil.getCurrentSessionId()
        ));
    }

    @GetMapping("/api/admin/auth-test")
    public ResponseEntity<Map<String, Object>> adminTest() {
        return ResponseEntity.ok(Map.of(
                "message", "관리자 API 접근 성공"
        ));
    }

    private void saveAdjectiveIfNotExists(String word) {
        List<NicknameAdjective> adjectives = adjectiveRepository.findByIsActiveTrue();

        boolean exists = adjectives.stream()
                .anyMatch(adjective -> adjective.getWord().equals(word));

        if (!exists) {
            adjectiveRepository.save(NicknameAdjective.create(word));
        }
    }

    private void saveNounIfNotExists(String word) {
        List<NicknameNoun> nouns = nounRepository.findByIsActiveTrue();

        boolean exists = nouns.stream()
                .anyMatch(noun -> noun.getWord().equals(word));

        if (!exists) {
            nounRepository.save(NicknameNoun.create(word));
        }
    }
}