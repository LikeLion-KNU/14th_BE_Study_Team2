package com.example.community.domain.user.repository;

import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * [메서드명 규칙으로 쿼리 자동 생성 — Spring Data JPA Query Method]
 *
 * Spring Data가 메서드 이름을 분석해서 SQL을 만들어줌.
 * 규칙: find|count|exists + By + 필드명 + 조건(And|Or|Not|Like|GreaterThan 등)
 *
 * findByStudentId(Long studentId)
 *   → SELECT * FROM users WHERE student_id = ? LIMIT 1
 *
 * existsByStudentId(Long studentId)
 *   → SELECT COUNT(*) > 0 FROM users WHERE student_id = ?
 *   → boolean 반환 (존재 여부만 확인, 데이터 조회 없이 효율적)
 *
 * existsByNickname(String nickname)
 *   → SELECT COUNT(*) > 0 FROM users WHERE nickname = ?
 *
 * findByStatus(UserStatus status, Pageable pageable)
 *   → SELECT * FROM users WHERE status = ? LIMIT ? OFFSET ?
 *   → Page<User> 반환: 조회된 목록 + 전체 개수 + 페이지 정보를 한 번에 담음
 *   → Pageable: Service에서 PageRequest.of(page, size)로 만들어서 전달
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByStudentId(Long studentId);

    boolean existsByStudentId(Long studentId);

    boolean existsByNickname(String nickname);

    // Admin 기능에서 추가: 가입 대기 회원 목록 페이지네이션 조회
    Page<User> findByStatus(UserStatus status, Pageable pageable);
}
