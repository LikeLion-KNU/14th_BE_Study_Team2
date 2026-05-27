package com.example.community.domain.post.repository;

import com.example.community.domain.post.entity.Post;
import com.example.community.domain.post.enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * countByUser_IdAndStatusNot(Long userId, PostStatus status):
 *
 * 메서드명 분해:
 *   count   → SELECT COUNT(*)
 *   By      → WHERE
 *   User_Id → post.user 연관관계의 id 필드 (user_id 컬럼)
 *   And     → AND
 *   StatusNot → status != ?
 *
 * 생성 SQL: SELECT COUNT(*) FROM posts WHERE user_id = ? AND status != ?
 * 사용처: 회원 상세 조회 시 삭제되지 않은 게시글 수 계산
 *   → countByUser_IdAndStatusNot(userId, PostStatus.DELETED)
 */
public interface PostRepository extends JpaRepository<Post, Long> {
    long countByUser_IdAndStatusNot(Long userId, PostStatus status);
}
