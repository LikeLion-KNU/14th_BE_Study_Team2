package com.example.community.domain.admin.service;

import com.example.community.domain.admin.dto.response.PendingUserResponse;
import com.example.community.domain.admin.dto.response.UserDetailResponse;
import com.example.community.domain.post.repository.CommentRepository;
import com.example.community.domain.post.repository.PostRepository;
import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.enums.UserStatus;
import com.example.community.domain.user.repository.AdminRepository;
import com.example.community.domain.user.repository.UserRejectionRepository;
import com.example.community.domain.user.repository.UserSanctionRepository;
import com.example.community.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import com.example.community.domain.admin.dto.response.ApproveResponse;
import com.example.community.domain.admin.dto.response.RejectResponse;
import com.example.community.domain.user.entity.Admin;
import com.example.community.domain.user.enums.AdminLevel;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock UserRepository userRepository;
    @Mock AdminRepository adminRepository;
    @Mock UserRejectionRepository userRejectionRepository;
    @Mock UserSanctionRepository userSanctionRepository;
    @Mock PostRepository postRepository;
    @Mock CommentRepository commentRepository;

    @InjectMocks AdminService adminService;

    User pendingUser;

    @BeforeEach
    void setUp() {
        pendingUser = User.create(20201234L, "pw", "홍길동", "경북대학교", "닉네임1", "http://cert.url");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getPendingUsers_returnsPendingUserPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(List.of(pendingUser), pageable, 1);
        when(userRepository.findByStatus(UserStatus.PENDING, pageable)).thenReturn(userPage);

        Page<PendingUserResponse> result = adminService.getPendingUsers(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("홍길동");
        assertThat(result.getContent().get(0).getStudentId()).isEqualTo(20201234L);
    }

    @Test
    void getUserDetail_returnsUserDetailWithCounts() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(pendingUser));
        when(postRepository.countByUser_IdAndStatusNot(userId, com.example.community.domain.post.enums.PostStatus.DELETED)).thenReturn(3L);
        when(commentRepository.countByUser_IdAndStatusNot(userId, com.example.community.domain.post.enums.CommentStatus.DELETED)).thenReturn(5L);

        UserDetailResponse result = adminService.getUserDetail(userId);

        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getPostCount()).isEqualTo(3L);
        assertThat(result.getCommentCount()).isEqualTo(5L);
    }

    @Test
    void getUserDetail_userNotFound_throwsCustomException() {
        when(userRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> adminService.getUserDetail(99L))
            .isInstanceOf(com.example.community.common.exception.CustomException.class);
    }

    @Test
    void approveUser_changesStatusToApproved() {
        User user = User.create(20201234L, "pw", "홍길동", "경북대학교", "닉네임1", "http://cert.url");
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

        ApproveResponse result = adminService.approveUser(1L);

        assertThat(result.getStatus()).isEqualTo("APPROVED");
        assertThat(result.getApprovedAt()).isNotNull();
    }

    @Test
    void rejectUser_changesStatusAndSavesRejection() {
        com.example.community.security.principal.CustomUserPrincipal principal =
            new com.example.community.security.principal.CustomUserPrincipal(
                99L, 20201234L, com.example.community.domain.user.enums.UserRole.ADMIN, "session-1");
        org.springframework.security.core.Authentication auth =
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                principal, null,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")));
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

        User user = User.create(20201234L, "pw", "홍길동", "경북대학교", "닉네임1", "http://cert.url");
        Admin admin = Admin.create(User.create(99L, "pw", "관리자", "경북대학교", "admin닉", "http://cert.url"), AdminLevel.STAFF);
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));
        when(adminRepository.findByUser_Id(99L)).thenReturn(java.util.Optional.of(admin));

        RejectResponse result = adminService.rejectUser(1L, "서류 미비");

        assertThat(result.getStatus()).isEqualTo("REJECTED");
    }
}
