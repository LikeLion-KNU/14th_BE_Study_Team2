package com.example.community.domain.post.entity;

import com.example.community.domain.common.BaseTimeEntity;
import com.example.community.domain.post.enums.PostStatus;
import com.example.community.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.util.ArrayList;
import java.util.List;

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

    // 1. 양방향 연관관계 추가: 게시글 상세 조회 시 댓글 목록을 바로 가져오기 위함
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // 2. 빌더 패턴 추가: 서비스 계층에서 안전하게 객체를 생성하기 위함
    @Builder
    public Post(User user, String title, String content) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.status = PostStatus.PUBLISHED;
    }

    // 3. 비즈니스 메서드 (수정): 엔티티 내부에서 데이터를 변경하는 '스프링다운' 방식
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // 4. 소프트 삭제 메서드: ERD의 deleted_at과 status를 동시에 관리
    public void delete() {
        this.status = PostStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }
}