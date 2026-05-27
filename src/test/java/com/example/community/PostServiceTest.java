package com.example.community;

import com.example.community.domain.post.dto.CommentRequest;
import com.example.community.domain.post.entity.Post;
import com.example.community.domain.post.enums.PostStatus;
import com.example.community.domain.post.repository.CommentRepository;
import com.example.community.domain.post.repository.PostRepository;
import com.example.community.domain.post.service.CommentService;
import com.example.community.domain.post.service.PostService;
import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PostServiceTest {

    @Autowired PostService postService;
    @Autowired CommentService commentService;
    @Autowired UserRepository userRepository;
    @Autowired PostRepository postRepository;
    @Autowired CommentRepository commentRepository;

    @Test
    @DisplayName("게시글 상태가 DELETED로 잘 변경되는지 확인")
    void 게시글_상태변경_테스트() {
        // 1. 정적 팩토리 메서드로 유저 생성 및 가입 승인
        User user = User.createPendingUser(
                20260001L,
                "raw_password",
                "김철수",
                "대학교",
                "테스터1",
                "http://..."
        );
        user.approve(LocalDateTime.now()); // 서비스 로직 통과를 위해 승인 처리
        userRepository.save(user);

        // 2. 게시글 생성
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .user(user)
                .build();
        postRepository.save(post);

        // 3. 삭제 실행 (소프트 삭제)
        post.delete();

        // 4. 검증
        assertThat(post.getStatus()).isEqualTo(PostStatus.DELETED);
        assertThat(post.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("게시글 삭제 시 게시글의 상태가 DELETED인지 확인 (통합 테스트)")
    void 게시글_댓글_통합_테스트() {
        // 1. 유저 생성 (✨ builder 대신 createPendingUser 적용 및 가입 승인)
        User user = User.createPendingUser(
                123L,
                "p",
                "n",
                "s",
                "nick",
                "c"
        );
        user.approve(LocalDateTime.now()); // 서비스 로직 통과를 위해 승인 처리
        userRepository.save(user);

        // 2. 게시글 생성
        Post post = Post.builder()
                .title("제목")
                .content("내용")
                .user(user)
                .build();
        postRepository.save(post);

        // 3. 댓글 생성 (댓글 요청 DTO 구조에 맞게 연동)
        CommentRequest commentReq = new CommentRequest("댓글내용", user.getId());
        Long commentId = commentService.create(post.getId(), commentReq);

        // 4. 게시글 삭제 (소프트 삭제)
        postService.delete(post.getId());

        // 5. 검증: 게시글이 DELETED인지 확인
        Post foundPost = postRepository.findById(post.getId()).get();
        assertThat(foundPost.getStatus()).isEqualTo(PostStatus.DELETED);
    }
}