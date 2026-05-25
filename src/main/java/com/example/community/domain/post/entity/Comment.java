package com.example.community.domain.post.entity;

import com.example.community.domain.common.BaseTimeEntity;
import com.example.community.domain.post.enums.CommentStatus;
import com.example.community.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status = CommentStatus.PUBLISHED;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 1. 빌더 패턴 추가: 서비스 계층에서 객체 생성 시 사용
    @Builder
    public Comment(Post post, User user, String content) {
        this.post = post;
        this.user = user;
        this.content = content;
        this.status = CommentStatus.PUBLISHED;
        // ✨ 양방향 연관관계 편의 로직 추가
        if (post != null) {
            post.getComments().add(this);
        }
    }

    // 2. 비즈니스 메서드 (수정): 댓글 내용 수정 로직
    public void update(String content) {
        this.content = content;
    }

    // 3. 소프트 삭제 메서드: 상태를 DELETED로 변경하고 삭제 시간 기록
    public void delete() {
        this.status = CommentStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }
}