package com.example.community.domain.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostCreateRequest {
    private String title;
    private String content;
    private Long userId; // 작성자 ID
}