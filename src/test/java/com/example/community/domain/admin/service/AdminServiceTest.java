package com.example.community.domain.admin.service;

import com.example.community.domain.admin.dto.response.PendingUserResponse;
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
import static org.mockito.Mockito.when;

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
}
