package com.example.community.domain.post.dto;

import com.example.community.domain.post.entity.Comment;
import com.example.community.domain.post.enums.CommentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private String nickname;
    private CommentStatus status;
    private LocalDateTime createdAt;

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getNickname(),
                comment.getStatus(),
                comment.getCreatedAt()
        );
    }
}