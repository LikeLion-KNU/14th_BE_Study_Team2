package com.example.community.domain.post.dto;

import lombok.AllArgsConstructor; // 추가
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {
    private String content;
    private Long userId;
}