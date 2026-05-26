package com.example.community.domain.user.repository;

import com.example.community.domain.user.entity.UserSanction;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 정지/강제탈퇴 이력 Repository
 *
 * BAN, FORCE_WITHDRAW 두 타입의 제재 이력을 저장
 * → AdminService.banUser(), withdrawUser()에서 save()로 INSERT
 *
 * 추후 "특정 유저의 제재 이력 조회" 기능이 생기면
 * findByUser_Id(Long userId) 같은 메서드를 여기에 추가
 */
public interface UserSanctionRepository extends JpaRepository<UserSanction, Long> {
}
