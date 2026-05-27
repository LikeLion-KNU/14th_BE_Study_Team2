package com.example.community.domain.post.repository;

import com.example.community.domain.post.entity.Comment;
import com.example.community.domain.post.enums.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByPostIdAndStatusNot(Long postId, CommentStatus status);
}