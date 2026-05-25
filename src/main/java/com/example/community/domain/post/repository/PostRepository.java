package com.example.community.domain.post.repository;

import com.example.community.domain.post.entity.Post;
import com.example.community.domain.post.enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    // 삭제되지 않은 게시글만 조회
    List<Post> findAllByStatusNot(PostStatus status);
}