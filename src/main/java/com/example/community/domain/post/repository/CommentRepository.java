package com.example.community.domain.post.repository;

import com.example.community.domain.post.entity.Comment;
import com.example.community.domain.post.enums.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * countByUser_IdAndStatusNot(Long userId, CommentStatus status):
 *
 * PostRepository와 동일한 패턴:
 *   생성 SQL: SELECT COUNT(*) FROM comments WHERE user_id = ? AND status != ?
 *   사용처: 회원 상세 조회 시 삭제되지 않은 댓글 수 계산
 *   → countByUser_IdAndStatusNot(userId, CommentStatus.DELETED)
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {
    long countByUser_IdAndStatusNot(Long userId, CommentStatus status);
}
