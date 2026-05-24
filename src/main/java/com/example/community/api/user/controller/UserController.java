package com.example.community.api.user.controller;

import com.example.community.api.user.dto.MyPageResponse;
import com.example.community.api.user.dto.UserUpdateRequest;
import com.example.community.api.user.service.UserService;
import com.example.community.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
// /api/users라는 주소로 시작함
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // [기능 1, 2] 내 정보(마이페이지) 조회
    // 보통 로그인 정보는 @AuthenticationPrincipal 또는 세션에서 ID를 가져옵니다.
    @GetMapping("/me")
    // 프론트엔트 요청 : GET 방식으로 http://서버주소/api/users/me 호출
    // 데이터 조회
    public ResponseEntity<MyPageResponse> getMyPage(/* @AuthenticationPrincipal Long userId */) {
        Long userId = SecurityUtil.getCurrentUserId(); // 💡 실제 구현 시에는 로그인된 유저 ID를 주입받아야 합니다.
        return ResponseEntity.ok(userService.getMyPage(userId));
    }

    // [기능 4, 5] 내 정보 수정 (닉네임 등)
    // 데이터 수정
    @PatchMapping("/me")
    public ResponseEntity<String> updateMyInfo(@RequestBody UserUpdateRequest requestDto) {
        Long userId = SecurityUtil.getCurrentUserId(); // 💡 로그인된 본인 ID만 전달됨 (자동 권한 체크)
        userService.updateUserInfo(userId, requestDto);
        return ResponseEntity.ok("회원 정보가 성공적으로 수정되었습니다.");
    }

    // [기능 5] 회원 탈퇴
    // 데이터 삭제
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMe() {
        Long userId = SecurityUtil.getCurrentUserId(); // 로그인된 본인 ID만 전달됨 (자동 권한 체크)
        userService.deleteUser(userId);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }
}