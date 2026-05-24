package com.example.community.domain.admin.controller;

import com.example.community.common.dto.ApiResponse;
import com.example.community.domain.admin.dto.request.RejectRequest;
import com.example.community.domain.admin.dto.request.BanRequest;
import com.example.community.domain.admin.dto.request.WithdrawRequest;
import com.example.community.domain.admin.dto.response.PendingUserResponse;
import com.example.community.domain.admin.dto.response.UserDetailResponse;
import com.example.community.domain.admin.dto.response.ApproveResponse;
import com.example.community.domain.admin.dto.response.RejectResponse;
import com.example.community.domain.admin.dto.response.BanResponse;
import com.example.community.domain.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users/pending")
    public ResponseEntity<ApiResponse<Page<PendingUserResponse>>> getPendingUsers(
        @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPendingUsers(pageable)));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetail(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserDetail(userId)));
    }

    @PatchMapping("/users/{userId}/approve")
    public ResponseEntity<ApiResponse<ApproveResponse>> approveUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.approveUser(userId)));
    }

    @PatchMapping("/users/{userId}/reject")
    public ResponseEntity<ApiResponse<RejectResponse>> rejectUser(
        @PathVariable Long userId,
        @RequestBody @Valid RejectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.rejectUser(userId, request.getReason())));
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        adminService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        adminService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{userId}/ban")
    public ResponseEntity<ApiResponse<BanResponse>> banUser(
        @PathVariable Long userId,
        @RequestBody @Valid BanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.banUser(userId, request.getReason())));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> withdrawUser(
        @PathVariable Long userId,
        @RequestBody @Valid WithdrawRequest request) {
        adminService.withdrawUser(userId, request.getReason());
        return ResponseEntity.noContent().build();
    }
}
