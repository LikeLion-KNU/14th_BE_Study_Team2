package com.example.community.domain.post.repository;

import com.example.community.domain.post.entity.Post;
import com.example.community.domain.post.enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    long countByUser_IdAndStatusNot(Long userId, PostStatus status);
}
