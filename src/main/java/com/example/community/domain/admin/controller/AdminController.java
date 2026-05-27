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

/**
 * [Controller 계층의 역할]
 * - HTTP 요청을 받아서 Service에 넘기고, Service 결과를 HTTP 응답으로 돌려주는 역할만 담당
 * - 비즈니스 로직(누가 승인됐는지, 어떤 조건인지)은 절대 여기서 처리하지 않음
 * - "HTTP를 모르는 Service"와 "비즈니스를 모르는 HTTP" 사이의 번역기 역할
 */

// @RestController = @Controller + @ResponseBody
// @Controller: 이 클래스가 Spring MVC의 컨트롤러임을 선언
// @ResponseBody: 메서드 반환값을 JSON으로 변환해서 HTTP 응답 바디에 넣음
@RestController

// 이 컨트롤러의 모든 엔드포인트 URL 앞에 /api/admin이 붙음
// 예: @GetMapping("/users/pending") → 실제 URL은 /api/admin/users/pending
@RequestMapping("/api/admin")

// final 필드를 생성자로 주입하는 코드를 Lombok이 자동 생성
// Spring이 AdminService 빈을 찾아서 생성자 파라미터로 주입해줌 (의존성 주입, DI)
@RequiredArgsConstructor
public class AdminController {

    // final + @RequiredArgsConstructor = 생성자 주입 패턴
    // 필드 주입(@Autowired)보다 권장됨 — 테스트 시 mock 주입이 쉽고, 불변성 보장
    private final AdminService adminService;

    /**
     * GET /api/admin/users/pending
     *
     * @PageableDefault: 클라이언트가 페이징 파라미터를 안 보내면 기본값 사용
     *   → ?page=0&size=20 과 동일한 효과
     *   → 클라이언트가 ?page=1&size=10 을 보내면 그 값으로 덮어씀
     *
     * Pageable: Spring Data가 ?page, ?size, ?sort 쿼리 파라미터를 자동으로 파싱해서 만들어줌
     *
     * ResponseEntity.ok(): HTTP 200 OK + 바디를 함께 반환하는 편의 메서드
     */
    @GetMapping("/users/pending")
    public ResponseEntity<ApiResponse<Page<PendingUserResponse>>> getPendingUsers(
        @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getPendingUsers(pageable)));
    }

    /**
     * GET /api/admin/users/{userId}
     *
     * @PathVariable: URL 경로의 {userId} 부분을 Long 타입으로 자동 변환해서 파라미터에 바인딩
     *   → GET /api/admin/users/42 요청 시 userId = 42L 이 들어옴
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetail(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserDetail(userId)));
    }

    /**
     * PATCH /api/admin/users/{userId}/approve
     *
     * PATCH: 리소스의 일부만 수정할 때 사용 (status 필드만 변경)
     * PUT:   리소스 전체를 교체할 때 사용
     * → 승인은 status 필드 하나만 바꾸므로 PATCH가 적합
     */
    @PatchMapping("/users/{userId}/approve")
    public ResponseEntity<ApiResponse<ApproveResponse>> approveUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.approveUser(userId)));
    }

    /**
     * PATCH /api/admin/users/{userId}/reject
     *
     * @RequestBody: HTTP 요청 바디의 JSON을 Java 객체로 역직렬화(deserialize)
     *   → {"reason": "서류 미비"} → RejectRequest(reason="서류 미비")
     *
     * @Valid: RejectRequest 안에 선언된 @NotBlank 등 Bean Validation 어노테이션을 실행
     *   → reason이 빈 문자열이면 400 Bad Request 자동 반환
     */
    @PatchMapping("/users/{userId}/reject")
    public ResponseEntity<ApiResponse<RejectResponse>> rejectUser(
        @PathVariable Long userId,
        @RequestBody @Valid RejectRequest request) {
        // record의 accessor: request.reason() (Lombok getter처럼 getReason()이 아님)
        return ResponseEntity.ok(ApiResponse.success(adminService.rejectUser(userId, request.reason())));
    }

    /**
     * DELETE /api/admin/posts/{postId}
     *
     * 실제로 DB에서 삭제하지 않음 (소프트 삭제)
     * → Service에서 status = DELETED로만 변경
     * → 204 No Content: 성공했지만 응답 바디가 없음을 의미
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        adminService.deletePost(postId);
        return ResponseEntity.noContent().build(); // HTTP 204
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        adminService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * PATCH /api/admin/users/{userId}/ban
     *
     * ban 종료 시각(endAt)은 서버에서 startAt + 7일로 고정 계산
     * → 클라이언트가 endAt을 보내지 않아도 됨, 조작 불가
     */
    @PatchMapping("/users/{userId}/ban")
    public ResponseEntity<ApiResponse<BanResponse>> banUser(
        @PathVariable Long userId,
        @RequestBody @Valid BanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.banUser(userId, request.reason())));
    }

    /**
     * DELETE /api/admin/users/{userId}
     *
     * HTTP DELETE인데 @RequestBody가 있는 이유:
     * → 탈퇴 사유(reason)를 저장해야 하는데, DELETE는 원래 바디가 없음
     * → REST 표준에 어긋나지만 실무에서 reason 저장이 필요할 때 흔히 쓰는 패턴
     * → 대안: reason을 쿼리 파라미터로 받거나, PATCH /users/{userId}/withdraw 로 변경
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> withdrawUser(
        @PathVariable Long userId,
        @RequestBody @Valid WithdrawRequest request) {
        adminService.withdrawUser(userId, request.reason());
        return ResponseEntity.noContent().build();
    }
}
