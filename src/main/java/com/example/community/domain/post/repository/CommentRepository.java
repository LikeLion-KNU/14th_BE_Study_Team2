package com.example.community.domain.post.repository;

import com.example.community.domain.post.entity.Comment;
import com.example.community.domain.post.enums.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    long countByUser_IdAndStatusNot(Long userId, CommentStatus status);
}
