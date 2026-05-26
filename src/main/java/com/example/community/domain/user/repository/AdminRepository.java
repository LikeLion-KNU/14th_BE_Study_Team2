package com.example.community.domain.user.repository;

import com.example.community.domain.user.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * [Spring Data JPA Repository]
 *
 * JpaRepository<Admin, Long> 를 상속하면:
 * → findById, save, delete, findAll 등 기본 CRUD 메서드를 Spring이 자동으로 구현
 * → 직접 SQL이나 구현 코드를 작성할 필요 없음. 인터페이스만 선언하면 됨.
 *
 * findByUser_Id(Long userId):
 * → 메서드 이름을 Spring Data가 파싱해서 자동으로 쿼리 생성
 * → "User_Id" → Admin 엔티티의 user 필드(연관관계)의 id 필드를 탐색
 * → 생성 SQL: SELECT * FROM admins WHERE user_id = ?
 *
 * Optional<Admin>: 결과가 없을 수 있음을 명시
 * → null 반환 대신 Optional.empty()로 안전하게 처리
 * → Service에서 .orElseThrow()로 없을 때 예외 던짐
 */
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUser_Id(Long userId);
}
