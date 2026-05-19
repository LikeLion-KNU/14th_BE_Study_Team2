# Admin 기능 구현 설계

## 프로젝트 개요

- 프로젝트: 대학교 인증 익명 커뮤니티 백엔드 (라이클리온 백엔드 2팀)
- 담당: Admin 기능 (은혜님)
- 스택: Spring Boot 3.5, JPA/Hibernate, H2(개발), Spring Security, JWT, Lombok
- 진행 방식: 기능 단위 하네스 루프 (브레인스토밍 → 계획 → TDD → 구현 → 검증)

## 아키텍처 — 패키지 구조

`com.example.community` 패키지 기준으로 Admin 기능을 추가한다.

```
com.example.community/
  domain/
    admin/
      controller/
        AdminController.java
      service/
        AdminService.java
      dto/
        request/
          RejectRequest.java
          BanRequest.java
          WithdrawRequest.java
        response/
          PendingUserResponse.java
          UserDetailResponse.java
          ApproveResponse.java
          RejectResponse.java
          BanResponse.java
    user/
      repository/
        UserRepository.java
        AdminRepository.java
        UserRejectionRepository.java
        UserSanctionRepository.java
    post/
      repository/
        PostRepository.java
        CommentRepository.java
    common/
      dto/
        ApiResponse.java
```

### 3계층 구조 역할

| 계층 | 클래스 | 역할 |
|------|--------|------|
| Controller | AdminController | HTTP 요청/응답 처리, URL 매핑 |
| Service | AdminService | 비즈니스 로직, 트랜잭션 관리 |
| Repository | UserRepository 등 | DB 쿼리, Spring Data JPA |

## API 명세 (Admin 담당 8개)

| # | 기능 | Method | URL | Request Body | Response |
|---|------|--------|-----|-------------|---------|
| 1 | 가입 대기 회원 조회 | GET | /api/admin/users/pending | - | 페이지네이션 목록 |
| 2 | 회원 상세 조회 | GET | /api/admin/users/{userId} | - | 상세 정보 + postCount, commentCount |
| 3 | 가입 승인 | PATCH | /api/admin/users/{userId}/approve | - | userId, status, approvedAt |
| 4 | 가입 반려 | PATCH | /api/admin/users/{userId}/reject | { reason } | userId, status |
| 5 | 게시글 삭제 | DELETE | /api/admin/posts/{postId} | - | 204 No Content |
| 6 | 댓글 삭제 | DELETE | /api/admin/comments/{commentId} | - | 204 No Content |
| 7 | 회원 정지 | PATCH | /api/admin/users/{userId}/ban | { reason } | userId, status, startAt, endAt |
| 8 | 강제 탈퇴 | DELETE | /api/admin/users/{userId} | { reason } | 204 No Content |

### 공통 응답 포맷

```json
// 성공
{ "success": true, "data": {}, "message": "요청 성공" }

// 실패
{ "success": false, "error_code": "ERROR_CODE", "message": "에러 설명" }
```

### 인증

- JWT Bearer 토큰 필수 (모든 Admin API)
- Admin 권한 확인 필요 (403 반환)

## ERD 요약 (Admin 관련 테이블)

| 테이블 | 역할 |
|--------|------|
| users | 회원 정보 + status(PENDING/APPROVED/REJECTED/BANNED) |
| admins | 관리자 전용 정보 (user_id FK) |
| user_rejections | 가입 반려 이력 (reason 저장) |
| user_sanctions | 정지/강제탈퇴 이력 (type: BAN / FORCE_WITHDRAW) |
| posts | 게시글 (소프트 삭제: status=DELETED, deleted_at) |
| comments | 댓글 (소프트 삭제: status=DELETED, deleted_at) |

## 구현 순서 (난이도 낮은 것부터)

| 순서 | 기능 | 배울 Spring 개념 |
|------|------|-----------------|
| 1 | 가입 대기 회원 조회 | @GetMapping, Repository, DTO 변환, Pageable |
| 2 | 회원 상세 조회 | @PathVariable, 연관관계 조회, count 쿼리 |
| 3 | 가입 승인 | @PatchMapping, @Transactional, 엔티티 상태 변경 |
| 4 | 가입 반려 | 연관 테이블에 이력 저장 (UserRejection 생성) |
| 5 | 게시글 삭제 | @DeleteMapping, 소프트 삭제 패턴 |
| 6 | 댓글 삭제 | 소프트 삭제 반복으로 패턴 굳히기 |
| 7 | 회원 정지 | 복합 로직: 상태 변경 + UserSanction 이력 저장 |
| 8 | 강제 탈퇴 | 복합 로직 마무리 + FORCE_WITHDRAW 타입 |

## 기능당 하네스 루프

매 기능마다 아래 순서를 반복한다:

```
1. 브레인스토밍  — 어떤 데이터가 필요한가? 어떤 예외가 있는가?
2. 계획 작성    — Controller/Service/Repository 역할 분담 명세
3. TDD         — 테스트 먼저 작성 (동작 명세 역할)
4. 구현         — 실제 코드, 각 레이어마다 Spring 개념 설명 포함
5. 검증         — 테스트 통과 + Swagger UI 실제 호출 확인
```

## 에러 처리

| 상황 | HTTP 코드 | error_code |
|------|-----------|-----------|
| 관리자 권한 없음 | 403 | FORBIDDEN |
| 존재하지 않는 사용자 | 404 | USER_NOT_FOUND |
| 존재하지 않는 게시글 | 404 | POST_NOT_FOUND |
| 존재하지 않는 댓글 | 404 | COMMENT_NOT_FOUND |
| 탈퇴 사유 미입력 | 400 | BAD_REQUEST |
