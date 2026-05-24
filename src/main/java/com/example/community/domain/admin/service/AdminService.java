package com.example.community.domain.admin.service;

import com.example.community.common.exception.CustomException;
import com.example.community.common.exception.ErrorCode;
import com.example.community.domain.admin.dto.response.PendingUserResponse;
import com.example.community.domain.post.repository.CommentRepository;
import com.example.community.domain.post.repository.PostRepository;
import com.example.community.domain.user.entity.Admin;
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

    private Admin findCurrentAdmin() {
        Long adminUserId = SecurityUtil.getCurrentUserId();
        return adminRepository.findByUser_Id(adminUserId)
            .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));
    }
}
