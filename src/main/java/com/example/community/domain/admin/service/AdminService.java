package com.example.community.domain.admin.service;

import com.example.community.common.exception.CustomException;
import com.example.community.common.exception.ErrorCode;
import com.example.community.domain.admin.dto.response.PendingUserResponse;
import com.example.community.domain.admin.dto.response.UserDetailResponse;
import com.example.community.domain.admin.dto.response.ApproveResponse;
import com.example.community.domain.admin.dto.response.RejectResponse;
import com.example.community.domain.admin.dto.response.BanResponse;
import com.example.community.domain.post.entity.Comment;
import com.example.community.domain.post.entity.Post;
import com.example.community.domain.post.enums.CommentStatus;
import com.example.community.domain.post.enums.PostStatus;
import com.example.community.domain.post.repository.CommentRepository;
import com.example.community.domain.post.repository.PostRepository;
import com.example.community.domain.user.entity.Admin;
import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.entity.UserRejection;
import com.example.community.domain.user.entity.UserSanction;
import com.example.community.domain.user.enums.SanctionType;
import com.example.community.domain.user.repository.AdminRepository;
import com.example.community.domain.user.repository.UserRejectionRepository;
import com.example.community.domain.user.repository.UserSanctionRepository;
import com.example.community.domain.user.repository.UserRepository;
import com.example.community.domain.user.enums.UserStatus;
import com.example.community.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * [Service 계층의 역할]
 * - 비즈니스 로직 전담: "승인이란 무엇인가", "정지 기간은 얼마인가" 같은 규칙이 여기에 있음
 * - 트랜잭션 관리: DB 작업의 원자성(all or nothing) 보장
 * - Controller는 HTTP만, Repository는 쿼리만 — 비즈니스 판단은 Service만
 */

// @Service: Spring이 이 클래스를 빈으로 등록. @Component의 특수화 버전 (의미 명확화 목적)
@Service
@RequiredArgsConstructor

/**
 * [@Transactional(readOnly = true) 클래스 레벨 전략]
 *
 * 이 클래스의 모든 메서드는 기본적으로 readOnly 트랜잭션으로 실행됨.
 *
 * readOnly = true 의 효과:
 * 1. Hibernate가 영속성 컨텍스트의 스냅샷을 저장하지 않음 → 메모리 절약, 속도 향상
 * 2. Dirty Checking(변경 감지) 생략 → 트랜잭션 종료 시 UPDATE 쿼리 실행 안 함
 * 3. DB에 따라 read replica로 자동 라우팅 가능 (Scale out 대비)
 *
 * 쓰기 메서드는 @Transactional로 오버라이드해서 쓰기 가능 트랜잭션으로 전환함.
 */
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final UserRejectionRepository userRejectionRepository;
    private final UserSanctionRepository userSanctionRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /**
     * 가입 대기 회원 목록 조회
     *
     * Page<User> → Page<PendingUserResponse> 변환:
     * .map(PendingUserResponse::from) 은 각 User 객체에 from() 팩토리 메서드를 적용
     * → Stream의 .map()과 동일한 개념. Page가 내부적으로 스트림을 돌려줌
     *
     * readOnly 트랜잭션 상속 (별도 @Transactional 없음)
     */
    public Page<PendingUserResponse> getPendingUsers(Pageable pageable) {
        return userRepository.findByStatus(UserStatus.PENDING, pageable)
            .map(PendingUserResponse::from);
    }

    /**
     * 회원 상세 조회
     *
     * orElseThrow(): Optional이 비어있으면 예외 던짐
     * → findById()는 Optional<User>를 반환. 없는 ID면 Optional.empty()
     * → CustomException이 GlobalExceptionHandler에서 잡혀 404 응답으로 변환됨
     */
    public UserDetailResponse getUserDetail(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        long postCount = postRepository.countByUser_IdAndStatusNot(userId, PostStatus.DELETED);
        long commentCount = commentRepository.countByUser_IdAndStatusNot(userId, CommentStatus.DELETED);
        return new UserDetailResponse(
            user.getId(), user.getStudentId(), user.getNickname(), user.getName(),
            user.getSchool(), user.getStatus().name(), user.getRole().name(),
            user.getCreatedAt(), user.getApprovedAt(), postCount, commentCount
        );
    }

    /**
     * 가입 승인
     *
     * [@Transactional 쓰기 오버라이드 + JPA Dirty Checking]
     *
     * user.approve()를 호출해도 save()가 없다. 어떻게 DB에 반영될까?
     *
     * 트랜잭션이 시작될 때 JPA는 조회한 엔티티의 스냅샷(최초 상태)을 찍어둠.
     * 트랜잭션이 끝날 때(메서드 return 직후) 현재 상태와 스냅샷을 비교(Dirty Checking).
     * status 필드가 바뀌었으면 → UPDATE users SET status=?, approved_at=? WHERE user_id=? 자동 실행.
     *
     * save()는 새 엔티티를 INSERT할 때만 명시적으로 필요함.
     */
    @Transactional
    public ApproveResponse approveUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.approve(); // status = APPROVED, approvedAt = now() — save() 없어도 반영됨
        return new ApproveResponse(user.getId(), user.getStatus().name(), user.getApprovedAt());
    }

    /**
     * 가입 반려
     *
     * 하나의 트랜잭션 안에서 두 가지 DB 작업:
     * 1. user.reject() → Dirty Checking으로 UPDATE (save 불필요)
     * 2. userRejectionRepository.save() → 새 행 INSERT (새 엔티티라 save 필요)
     *
     * 중간에 예외가 나면 두 작업 모두 롤백 → 부분 저장 없음
     */
    @Transactional
    public RejectResponse rejectUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Admin admin = findCurrentAdmin(); // JWT에서 현재 로그인한 관리자 조회
        user.reject();
        userRejectionRepository.save(UserRejection.of(user, admin, reason)); // 반려 이력 INSERT
        return new RejectResponse(user.getId(), user.getStatus().name());
    }

    /**
     * 게시글 소프트 삭제
     *
     * [소프트 삭제 패턴]
     * DELETE SQL을 실행하지 않고 status = DELETED, deleted_at = now() 로만 변경.
     * → 데이터 보존 (신고/감사 목적), 실수 복구 가능
     * → void 반환: Controller에서 204 No Content로 응답
     */
    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        post.softDelete(); // status = DELETED, deletedAt = now() — Dirty Checking으로 UPDATE
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        comment.softDelete();
    }

    /**
     * 회원 정지
     *
     * ban 종료 시각은 서버에서 계산 (클라이언트 조작 불가)
     * → startAt = 지금, endAt = 지금 + 7일 고정
     *
     * userSanctionRepository.save(): UserSanction은 새 엔티티 → INSERT 필요
     */
    @Transactional
    public BanResponse banUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Admin admin = findCurrentAdmin();
        LocalDateTime startAt = LocalDateTime.now();
        LocalDateTime endAt = startAt.plusDays(7);
        user.ban(); // status = BANNED (Dirty Checking)
        userSanctionRepository.save(UserSanction.of(user, admin, SanctionType.BAN, reason, startAt, endAt));
        return new BanResponse(user.getId(), user.getStatus().name(), startAt, endAt);
    }

    /**
     * 강제 탈퇴
     *
     * UserStatus에 WITHDRAWN이 없어서 BANNED로 저장.
     * 강제탈퇴와 일반정지의 구분은 user_sanctions.type = FORCE_WITHDRAW 로만 가능.
     * → 추후 WITHDRAWN 상태 추가 여부는 팀 논의 필요
     *
     * endAt = null: 강제탈퇴는 기간이 없음 (영구)
     */
    @Transactional
    public void withdrawUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Admin admin = findCurrentAdmin();
        LocalDateTime now = LocalDateTime.now();
        user.ban(); // BANNED 상태로 처리 (WITHDRAWN enum 없음)
        userSanctionRepository.save(UserSanction.of(user, admin, SanctionType.FORCE_WITHDRAW, reason, now, null));
    }

    /**
     * [SecurityUtil — JWT에서 현재 관리자 정보 꺼내기]
     *
     * JwtAuthenticationFilter가 요청마다:
     *   1. Authorization 헤더에서 토큰 추출
     *   2. 토큰 파싱 → CustomUserPrincipal(userId, role, ...) 생성
     *   3. SecurityContextHolder에 저장
     *
     * SecurityUtil.getCurrentUserId()는 SecurityContextHolder에서 userId를 꺼내줌.
     * → Controller 파라미터 없이 어디서든 현재 로그인 사용자 ID에 접근 가능.
     *
     * private 메서드: Service 내부에서만 사용하는 헬퍼
     */
    private Admin findCurrentAdmin() {
        Long adminUserId = SecurityUtil.getCurrentUserId();
        return adminRepository.findByUser_Id(adminUserId)
            .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));
    }
}
