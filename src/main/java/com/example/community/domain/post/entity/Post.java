package com.example.community.domain.post.entity;

import com.example.community.domain.common.BaseTimeEntity;
import com.example.community.domain.post.enums.PostStatus;
import com.example.community.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status = PostStatus.PUBLISHED;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void softDelete() {
        this.status = PostStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }

    // 테스트 전용 팩토리
    public static Post createForTest(User user, String title, String content) {
        Post post = new Post();
        post.user = user;
        post.title = title;
        post.content = content;
        post.status = PostStatus.PUBLISHED;
        return post;
    }
}