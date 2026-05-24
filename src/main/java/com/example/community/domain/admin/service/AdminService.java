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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final UserRejectionRepository userRejectionRepository;
    private final UserSanctionRepository userSanctionRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public Page<PendingUserResponse> getPendingUsers(Pageable pageable) {
        return userRepository.findByStatus(UserStatus.PENDING, pageable)
            .map(PendingUserResponse::from);
    }

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

    @Transactional
    public ApproveResponse approveUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.approve();
        return new ApproveResponse(user.getId(), user.getStatus().name(), user.getApprovedAt());
    }

    @Transactional
    public RejectResponse rejectUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Admin admin = findCurrentAdmin();
        user.reject();
        userRejectionRepository.save(UserRejection.of(user, admin, reason));
        return new RejectResponse(user.getId(), user.getStatus().name());
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        post.softDelete();
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        comment.softDelete();
    }

    @Transactional
    public BanResponse banUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Admin admin = findCurrentAdmin();
        LocalDateTime startAt = LocalDateTime.now();
        LocalDateTime endAt = startAt.plusDays(7);
        user.ban();
        userSanctionRepository.save(UserSanction.of(user, admin, SanctionType.BAN, reason, startAt, endAt));
        return new BanResponse(user.getId(), user.getStatus().name(), startAt, endAt);
    }

    @Transactional
    public void withdrawUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Admin admin = findCurrentAdmin();
        LocalDateTime now = LocalDateTime.now();
        user.ban();
        userSanctionRepository.save(UserSanction.of(user, admin, SanctionType.FORCE_WITHDRAW, reason, now, null));
    }

    private Admin findCurrentAdmin() {
        Long adminUserId = SecurityUtil.getCurrentUserId();
        return adminRepository.findByUser_Id(adminUserId)
            .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));
    }
}
