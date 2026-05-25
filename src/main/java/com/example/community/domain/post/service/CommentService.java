package com.example.community.domain.post.service;

import com.example.community.domain.post.dto.CommentRequest;
import com.example.community.domain.post.dto.CommentResponse;
import com.example.community.domain.post.entity.Comment;
import com.example.community.domain.post.entity.Post;
import com.example.community.domain.post.enums.CommentStatus;
import com.example.community.domain.post.repository.CommentRepository;
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
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long create(Long postId, CommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .user(user)
                .build();

        return commentRepository.save(comment).getId();
    }

    public List<CommentResponse> findAllByPost(Long postId) {
        return commentRepository.findAllByPostIdAndStatusNot(postId, CommentStatus.DELETED).stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void update(Long commentId, CommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        comment.update(request.getContent());
    }

    @Transactional
    public void delete(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
        comment.delete(); // Comment 엔티티의 상태를 DELETED로 변경하고 deletedAt 기록
    }
}