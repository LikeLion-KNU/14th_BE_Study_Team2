package com.example.community.domain.admin.controller;

import com.example.community.common.dto.ApiResponse;
import com.example.community.domain.admin.dto.response.PendingUserResponse;
import com.example.community.domain.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
