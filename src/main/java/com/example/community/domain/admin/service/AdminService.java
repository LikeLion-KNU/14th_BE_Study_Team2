package com.example.community.domain.admin.service;

import com.example.community.common.exception.CustomException;
import com.example.community.common.exception.ErrorCode;
import com.example.community.domain.admin.dto.response.PendingUserResponse;
import com.example.community.domain.admin.dto.response.UserDetailResponse;
import com.example.community.domain.admin.dto.response.ApproveResponse;
import com.example.community.domain.post.enums.CommentStatus;
import com.example.community.domain.post.enums.PostStatus;
import com.example.community.domain.post.repository.CommentRepository;
import com.example.community.domain.post.repository.PostRepository;
import com.example.community.domain.user.entity.Admin;
import com.example.community.domain.user.entity.User;
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

    private Admin findCurrentAdmin() {
        Long adminUserId = SecurityUtil.getCurrentUserId();
        return adminRepository.findByUser_Id(adminUserId)
            .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));
    }
}
