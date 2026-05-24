package com.example.community.domain.admin.controller;

import com.example.community.domain.admin.dto.response.PendingUserResponse;
import com.example.community.domain.admin.dto.response.UserDetailResponse;
import com.example.community.domain.admin.service.AdminService;
import com.example.community.domain.user.repository.ActiveSessionRepository;
import com.example.community.domain.user.repository.UserRepository;
import com.example.community.security.config.SecurityConfig;
import com.example.community.security.handler.JwtAccessDeniedHandler;
import com.example.community.security.handler.JwtAuthenticationEntryPoint;
import com.example.community.security.jwt.JwtAuthenticationFilter;
import com.example.community.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.example.community.domain.admin.dto.response.ApproveResponse;
import com.example.community.domain.admin.dto.response.RejectResponse;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
class AdminControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AdminService adminService;
    @MockBean JwtTokenProvider jwtTokenProvider;
    @MockBean ActiveSessionRepository activeSessionRepository;
    @MockBean UserRepository userRepository;

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void getPendingUsers_returns200WithPage() throws Exception {
        PendingUserResponse response = new PendingUserResponse(1L, 20201234L, "홍길동", "경북대학교", "http://cert.url", null);
        when(adminService.getPendingUsers(any())).thenReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/admin/users/pending"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content[0].name").value("홍길동"));
    }

    @Test
    void getPendingUsers_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/users/pending"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void getUserDetail_returns200() throws Exception {
        UserDetailResponse response = new UserDetailResponse(1L, 20201234L, "닉네임1", "홍길동", "경북대학교", "PENDING", "USER", null, null, 3L, 5L);
        when(adminService.getUserDetail(1L)).thenReturn(response);

        mockMvc.perform(get("/api/admin/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("홍길동"))
            .andExpect(jsonPath("$.data.postCount").value(3));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void approveUser_returns200() throws Exception {
        ApproveResponse response = new ApproveResponse(1L, "APPROVED", java.time.LocalDateTime.now());
        when(adminService.approveUser(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/admin/users/1/approve"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void rejectUser_returns200() throws Exception {
        RejectResponse response = new RejectResponse(1L, "REJECTED");
        when(adminService.rejectUser(eq(1L), anyString())).thenReturn(response);

        mockMvc.perform(patch("/api/admin/users/1/reject")
                .contentType("application/json")
                .content("{\"reason\":\"서류 미비\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }
}
