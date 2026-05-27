package com.example.community.domain.user.repository;

import com.example.community.domain.user.entity.UserRejection;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 가입 반려 이력 Repository
 *
 * 별도 쿼리 메서드 없음 — JpaRepository 기본 제공 save()만 사용
 * → AdminService.rejectUser()에서 UserRejection 생성 후 save()로 INSERT
 *
 * 이력 테이블은 조회보다 저장이 주 목적이라 기본 메서드로 충분
 */
public interface UserRejectionRepository extends JpaRepository<UserRejection, Long> {
}
