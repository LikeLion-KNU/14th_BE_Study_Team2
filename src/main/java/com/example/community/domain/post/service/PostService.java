package com.example.community.domain.post.service;

import com.example.community.domain.post.dto.PostCreateRequest;
import com.example.community.domain.post.dto.PostResponse;
import com.example.community.domain.post.entity.Post;
import com.example.community.domain.post.enums.PostStatus;
import com.example.community.domain.post.repository.PostRepository;
import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long create(PostCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();

        return postRepository.save(post).getId();
    }

    public List<PostResponse> findAll() {
        // DELETED 상태가 아닌 게시글만 목록에 표시
        return postRepository.findAllByStatusNot(PostStatus.DELETED).stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());
    }

    public PostResponse findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        return PostResponse.from(post);
    }

    @Transactional
    public void update(Long id, PostCreateRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.update(request.getTitle(), request.getContent());
    }

    @Transactional
    public void delete(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        post.delete(); // 내부에서 status = DELETED 및 deletedAt 설정
    }
}