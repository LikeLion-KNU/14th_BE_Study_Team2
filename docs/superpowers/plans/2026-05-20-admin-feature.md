# Admin 기능 구현 계획

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Admin API 8개 엔드포인트를 Spring 3계층 구조(Controller/Service/Repository)로 구현하고, 각 기능마다 TDD 사이클을 적용한다.

**Architecture:** 공통 응답 포맷(`ApiResponse<T>`)과 예외 처리를 먼저 구성한 뒤, Repository → Entity 비즈니스 메서드 → Service/Controller 순으로 쌓는다. Controller 테스트는 `@WebMvcTest` + `@MockBean`으로 Service를 격리하고, Service 테스트는 `@ExtendWith(MockitoExtension.class)` + Mockito로 Repository를 격리한다.

**Tech Stack:** Java 17, Spring Boot 3.5, Spring Data JPA, Spring Security, H2, Lombok, JUnit 5, Mockito, MockMvc

---

## 생성/수정 파일 목록

**생성 — 공통**
- `src/main/java/com/example/community/common/dto/ApiResponse.java`
- `src/main/java/com/example/community/common/exception/ErrorCode.java`
- `src/main/java/com/example/community/common/exception/CustomException.java`
- `src/main/java/com/example/community/common/exception/GlobalExceptionHandler.java`
- `src/main/java/com/example/community/config/JpaConfig.java`
- *(SecurityConfig은 PR #4의 `security/config/SecurityConfig.java`를 사용 — 직접 생성하지 않음)*

**생성 — Repository**
- `src/main/java/com/example/community/domain/user/repository/UserRepository.java`
- `src/main/java/com/example/community/domain/user/repository/AdminRepository.java`
- `src/main/java/com/example/community/domain/user/repository/UserRejectionRepository.java`
- `src/main/java/com/example/community/domain/user/repository/UserSanctionRepository.java`
- `src/main/java/com/example/community/domain/post/repository/PostRepository.java`
- `src/main/java/com/example/community/domain/post/repository/CommentRepository.java`

**생성 — Admin 기능**
- `src/main/java/com/example/community/domain/admin/dto/request/RejectRequest.java`
- `src/main/java/com/example/community/domain/admin/dto/request/BanRequest.java`
- `src/main/java/com/example/community/domain/admin/dto/request/WithdrawRequest.java`
- `src/main/java/com/example/community/domain/admin/dto/response/PendingUserResponse.java`
- `src/main/java/com/example/community/domain/admin/dto/response/UserDetailResponse.java`
- `src/main/java/com/example/community/domain/admin/dto/response/ApproveResponse.java`
- `src/main/java/com/example/community/domain/admin/dto/response/RejectResponse.java`
- `src/main/java/com/example/community/domain/admin/dto/response/BanResponse.java`
- `src/main/java/com/example/community/domain/admin/service/AdminService.java`
- `src/main/java/com/example/community/domain/admin/controller/AdminController.java`

**수정 — Entity**
- `src/main/java/com/example/community/domain/user/entity/User.java`
- `src/main/java/com/example/community/domain/user/entity/Admin.java`
- `src/main/java/com/example/community/domain/user/entity/UserRejection.java`
- `src/main/java/com/example/community/domain/user/entity/UserSanction.java`
- `src/main/java/com/example/community/domain/post/entity/Post.java`
- `src/main/java/com/example/community/domain/post/entity/Comment.java`

**테스트**
- `src/test/java/com/example/community/common/exception/GlobalExceptionHandlerTest.java`
- `src/test/java/com/example/community/domain/user/entity/UserEntityTest.java`
- `src/test/java/com/example/community/domain/admin/service/AdminServiceTest.java`
- `src/test/java/com/example/community/domain/admin/controller/AdminControllerTest.java`

---

### Task 1: 공통 인프라 — ApiResponse / ErrorCode / CustomException / GlobalExceptionHandler

> **Spring 개념:** `@RestControllerAdvice`는 모든 Controller에서 발생하는 예외를 한 곳에서 잡아준다. `ApiResponse<T>`는 제네릭 래퍼로, 성공/실패 응답 형식을 통일한다.

**Files:**
- Create: `src/main/java/com/example/community/common/dto/ApiResponse.java`
- Create: `src/main/java/com/example/community/common/exception/ErrorCode.java`
- Create: `src/main/java/com/example/community/common/exception/CustomException.java`
- Create: `src/main/java/com/example/community/common/exception/GlobalExceptionHandler.java`
- Test: `src/test/java/com/example/community/common/exception/GlobalExceptionHandlerTest.java`

- [ ] **Step 1: 테스트 먼저 작성**

```java
// src/test/java/com/example/community/common/exception/GlobalExceptionHandlerTest.java
package com.example.community.common.exception;

import com.example.community.common.dto.ApiResponse;
import com.example.community.security.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
class GlobalExceptionHandlerTest {

    @Autowired MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/test/error")
        public void throwError() {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
    }

    @Test
    @WithMockUser
    void customException_returns404WithErrorBody() throws Exception {
        mockMvc.perform(get("/test/error"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errorCode").value("USER_NOT_FOUND"));
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

```bash
./gradlew test --tests "com.example.community.common.exception.GlobalExceptionHandlerTest" 2>&1 | tail -20
```
예상: FAILED — `ApiResponse`, `ErrorCode`, `CustomException`, `SecurityConfig` 클래스 없음

- [ ] **Step 3: ErrorCode 생성**

```java
// src/main/java/com/example/community/common/exception/ErrorCode.java
package com.example.community.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("존재하지 않는 사용자입니다."),
    POST_NOT_FOUND("존재하지 않는 게시글입니다."),
    COMMENT_NOT_FOUND("존재하지 않는 댓글입니다."),
    ADMIN_NOT_FOUND("관리자 정보를 찾을 수 없습니다."),
    FORBIDDEN("관리자 권한이 없습니다."),
    BAD_REQUEST("잘못된 요청입니다.");

    private final String message;
}
```

- [ ] **Step 4: CustomException 생성**

```java
// src/main/java/com/example/community/common/exception/CustomException.java
package com.example.community.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

- [ ] **Step 5: ApiResponse 생성**

```java
// src/main/java/com/example/community/common/dto/ApiResponse.java
package com.example.community.common.dto;

import com.example.community.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String errorCode;
    private final String message;

    private ApiResponse(boolean success, T data, String errorCode, String message) {
        this.success = success;
        this.data = data;
        this.errorCode = errorCode;
        this.message = message;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, "요청 성공");
    }

    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, errorCode.name(), errorCode.getMessage());
    }
}
```

- [ ] **Step 6: GlobalExceptionHandler 생성**

```java
// src/main/java/com/example/community/common/exception/GlobalExceptionHandler.java
package com.example.community.common.exception;

import com.example.community.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        HttpStatus status = switch (e.getErrorCode()) {
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case USER_NOT_FOUND, POST_NOT_FOUND, COMMENT_NOT_FOUND, ADMIN_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(ApiResponse.error(e.getErrorCode()));
    }
}
```

- [ ] **Step 7: 테스트 통과 확인**

> **NOTE:** SecurityConfig는 PR #4(`security/config/SecurityConfig.java`)에서 제공된다. 별도로 생성하지 않는다. `@EnableJpaAuditing`을 분리한 `JpaConfig.java`는 이미 존재해야 한다.

```bash
./gradlew test --tests "com.example.community.common.exception.GlobalExceptionHandlerTest" 2>&1 | tail -10
```
예상: BUILD SUCCESSFUL, 1 test passed

- [ ] **Step 8: 커밋**

```bash
git add src/main/java/com/example/community/common src/main/java/com/example/community/config src/test/java/com/example/community/common
git commit -m "feat: 공통 응답 포맷, 예외 처리 추가"
```

---

### Task 2: Repository 6개 생성

> **Spring 개념:** `JpaRepository<Entity, ID>`를 상속하면 `save()`, `findById()`, `findAll()`, `delete()` 등 기본 CRUD 메서드가 자동으로 생긴다. 추가 쿼리는 메서드 이름으로 자동 생성된다(e.g. `findByStatus`).

**Files:**
- Create: `src/main/java/com/example/community/domain/user/repository/UserRepository.java`
- Create: `src/main/java/com/example/community/domain/user/repository/AdminRepository.java`
- Create: `src/main/java/com/example/community/domain/user/repository/UserRejectionRepository.java`
- Create: `src/main/java/com/example/community/domain/user/repository/UserSanctionRepository.java`
- Create: `src/main/java/com/example/community/domain/post/repository/PostRepository.java`
- Create: `src/main/java/com/example/community/domain/post/repository/CommentRepository.java`

- [ ] **Step 1: UserRepository 생성**

```java
// src/main/java/com/example/community/domain/user/repository/UserRepository.java
package com.example.community.domain.user.repository;

import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Page<User> findByStatus(UserStatus status, Pageable pageable);
}
```

- [ ] **Step 2: AdminRepository 생성**

```java
// src/main/java/com/example/community/domain/user/repository/AdminRepository.java
package com.example.community.domain.user.repository;

import com.example.community.domain.user.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUser_Id(Long userId);
}
```

- [ ] **Step 3: UserRejectionRepository 생성**

```java
// src/main/java/com/example/community/domain/user/repository/UserRejectionRepository.java
package com.example.community.domain.user.repository;

import com.example.community.domain.user.entity.UserRejection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRejectionRepository extends JpaRepository<UserRejection, Long> {
}
```

- [ ] **Step 4: UserSanctionRepository 생성**

```java
// src/main/java/com/example/community/domain/user/repository/UserSanctionRepository.java
package com.example.community.domain.user.repository;

import com.example.community.domain.user.entity.UserSanction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSanctionRepository extends JpaRepository<UserSanction, Long> {
}
```

- [ ] **Step 5: PostRepository 생성**

```java
// src/main/java/com/example/community/domain/post/repository/PostRepository.java
package com.example.community.domain.post.repository;

import com.example.community.domain.post.entity.Post;
import com.example.community.domain.post.enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    long countByUser_IdAndStatusNot(Long userId, PostStatus status);
}
```

- [ ] **Step 6: CommentRepository 생성**

```java
// src/main/java/com/example/community/domain/post/repository/CommentRepository.java
package com.example.community.domain.post.repository;

import com.example.community.domain.post.entity.Comment;
import com.example.community.domain.post.enums.CommentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    long countByUser_IdAndStatusNot(Long userId, CommentStatus status);
}
```

- [ ] **Step 7: 컴파일 확인**

```bash
./gradlew compileJava 2>&1 | tail -10
```
예상: BUILD SUCCESSFUL

- [ ] **Step 8: 커밋**

```bash
git add src/main/java/com/example/community/domain/user/repository src/main/java/com/example/community/domain/post/repository
git commit -m "feat: JPA Repository 인터페이스 6개 추가"
```

---

### Task 3: Entity 비즈니스 메서드 + Factory 메서드 추가

> **Spring 개념:** JPA 엔티티는 setter 대신 의도가 명확한 메서드(e.g. `approve()`)를 쓴다. `@Transactional` 안에서 엔티티를 수정하면 트랜잭션 종료 시 JPA가 자동으로 UPDATE 쿼리를 날린다(더티 체킹).

**Files:**
- Modify: `src/main/java/com/example/community/domain/user/entity/User.java`
- Modify: `src/main/java/com/example/community/domain/user/entity/Admin.java`
- Modify: `src/main/java/com/example/community/domain/user/entity/UserRejection.java`
- Modify: `src/main/java/com/example/community/domain/user/entity/UserSanction.java`
- Modify: `src/main/java/com/example/community/domain/post/entity/Post.java`
- Modify: `src/main/java/com/example/community/domain/post/entity/Comment.java`
- Test: `src/test/java/com/example/community/domain/user/entity/UserEntityTest.java`

- [ ] **Step 1: 테스트 먼저 작성**

```java
// src/test/java/com/example/community/domain/user/entity/UserEntityTest.java
package com.example.community.domain.user.entity;

import com.example.community.domain.user.enums.UserStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void approve_changesStatusToApproved() {
        User user = User.create(20201234L, "pw", "홍길동", "경북대학교", "닉네임1", "http://cert.url");
        user.approve();
        assertThat(user.getStatus()).isEqualTo(UserStatus.APPROVED);
        assertThat(user.getApprovedAt()).isNotNull();
    }

    @Test
    void reject_changesStatusToRejected() {
        User user = User.create(20201234L, "pw", "홍길동", "경북대학교", "닉네임1", "http://cert.url");
        user.reject();
        assertThat(user.getStatus()).isEqualTo(UserStatus.REJECTED);
    }

    @Test
    void ban_changesStatusToBanned() {
        User user = User.create(20201234L, "pw", "홍길동", "경북대학교", "닉네임1", "http://cert.url");
        user.ban();
        assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

```bash
./gradlew test --tests "com.example.community.domain.user.entity.UserEntityTest" 2>&1 | tail -10
```
예상: FAILED — `User.create()`, `approve()`, `reject()`, `ban()` 메서드 없음

- [ ] **Step 3: User.java에 메서드 추가**

기존 파일 끝에 닫는 `}` 바로 앞에 추가:

```java
    // --- 팩토리 메서드 ---
    public static User create(Long studentId, String hashedPw, String name,
                              String school, String nickname, String certificateUrl) {
        User user = new User();
        user.studentId = studentId;
        user.hashedPw = hashedPw;
        user.name = name;
        user.school = school;
        user.nickname = nickname;
        user.certificateUrl = certificateUrl;
        return user;
    }

    // --- 비즈니스 메서드 ---
    public void approve() {
        this.status = UserStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = UserStatus.REJECTED;
    }

    public void ban() {
        this.status = UserStatus.BANNED;
    }
```

- [ ] **Step 4: Admin.java에 팩토리 메서드 추가**

```java
    public static Admin create(User user, AdminLevel adminLevel) {
        Admin admin = new Admin();
        admin.user = user;
        admin.adminLevel = adminLevel;
        admin.grantedAt = LocalDateTime.now();
        return admin;
    }
```

- [ ] **Step 5: UserRejection.java에 팩토리 메서드 추가**

```java
    public static UserRejection of(User user, Admin admin, String reason) {
        UserRejection rejection = new UserRejection();
        rejection.user = user;
        rejection.admin = admin;
        rejection.reason = reason;
        return rejection;
    }
```

- [ ] **Step 6: UserSanction.java에 팩토리 메서드 추가**

```java
    public static UserSanction of(User user, Admin admin, SanctionType type,
                                   String reason, LocalDateTime startAt, LocalDateTime endAt) {
        UserSanction sanction = new UserSanction();
        sanction.user = user;
        sanction.admin = admin;
        sanction.type = type;
        sanction.reason = reason;
        sanction.startAt = startAt;
        sanction.endAt = endAt;
        return sanction;
    }
```

- [ ] **Step 7: Post.java에 softDelete 추가**

```java
    public void softDelete() {
        this.status = PostStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }
```

- [ ] **Step 8: Comment.java에 softDelete 추가**

```java
    public void softDelete() {
        this.status = CommentStatus.DELETED;
        this.deletedAt = LocalDateTime.now();
    }
```

- [ ] **Step 9: 테스트 통과 확인**

```bash
./gradlew test --tests "com.example.community.domain.user.entity.UserEntityTest" 2>&1 | tail -10
```
예상: BUILD SUCCESSFUL, 3 tests passed

- [ ] **Step 10: 커밋**

```bash
git add src/main/java/com/example/community/domain
git commit -m "feat: Entity 비즈니스 메서드 및 팩토리 메서드 추가"
```

---

### Task 4: DTO + AdminService/AdminController 뼈대 + 기능1 — 가입 대기 회원 조회

> **Spring 개념:**
> - `@RestController` = `@Controller` + `@ResponseBody`. 메서드 반환값이 자동으로 JSON으로 직렬화된다.
> - `@RequestMapping("/api/admin")` 은 클래스 레벨에서 URL prefix를 설정한다.
> - `Pageable`은 Spring이 `?page=0&size=20` 쿼리 파라미터를 자동으로 객체로 변환해준다.

**Files:**
- Create: `src/main/java/com/example/community/domain/admin/dto/response/PendingUserResponse.java`
- Create: `src/main/java/com/example/community/domain/admin/service/AdminService.java`
- Create: `src/main/java/com/example/community/domain/admin/controller/AdminController.java`
- Test: `src/test/java/com/example/community/domain/admin/service/AdminServiceTest.java`
- Test: `src/test/java/com/example/community/domain/admin/controller/AdminControllerTest.java`

- [ ] **Step 1: 서비스 테스트 먼저 작성**

```java
// src/test/java/com/example/community/domain/admin/service/AdminServiceTest.java
package com.example.community.domain.admin.service;

import com.example.community.domain.admin.dto.response.PendingUserResponse;
import com.example.community.domain.post.repository.CommentRepository;
import com.example.community.domain.post.repository.PostRepository;
import com.example.community.domain.user.entity.Admin;
import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.enums.AdminLevel;
import com.example.community.domain.user.enums.UserStatus;
import com.example.community.domain.user.repository.AdminRepository;
import com.example.community.domain.user.repository.UserRejectionRepository;
import com.example.community.domain.user.repository.UserSanctionRepository;
import com.example.community.domain.user.repository.UserRepository;
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

    private User pendingUser;

    @BeforeEach
    void setUp() {
        pendingUser = User.create(20201234L, "pw", "홍길동", "경북대학교", "닉네임1", "http://cert.url");
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
```

- [ ] **Step 2: 컨트롤러 테스트 먼저 작성**

```java
// src/test/java/com/example/community/domain/admin/controller/AdminControllerTest.java
package com.example.community.domain.admin.controller;

import com.example.community.security.config.SecurityConfig;
import com.example.community.domain.admin.dto.response.PendingUserResponse;
import com.example.community.domain.admin.service.AdminService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AdminService adminService;

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
    void getPendingUsers_withoutAuth_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users/pending"))
            .andExpect(status().isForbidden());
    }
}
```

- [ ] **Step 3: 테스트 실패 확인**

```bash
./gradlew test --tests "com.example.community.domain.admin.*" 2>&1 | tail -20
```
예상: FAILED — `PendingUserResponse`, `AdminService`, `AdminController` 없음

- [ ] **Step 4: PendingUserResponse 생성**

```java
// src/main/java/com/example/community/domain/admin/dto/response/PendingUserResponse.java
package com.example.community.domain.admin.dto.response;

import com.example.community.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PendingUserResponse {
    private Long userId;
    private Long studentId;
    private String name;
    private String school;
    private String certificateUrl;
    private LocalDateTime createdAt;

    public static PendingUserResponse from(User user) {
        return new PendingUserResponse(
            user.getId(), user.getStudentId(), user.getName(),
            user.getSchool(), user.getCertificateUrl(), user.getCreatedAt()
        );
    }
}
```

- [ ] **Step 5: AdminService 뼈대 + getPendingUsers 구현**

```java
// src/main/java/com/example/community/domain/admin/service/AdminService.java
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

    // 헬퍼: 현재 로그인한 관리자 엔티티 조회 (SecurityUtil 사용)
    private Admin findCurrentAdmin() {
        Long adminUserId = com.example.community.security.util.SecurityUtil.getCurrentUserId();
        return adminRepository.findByUser_Id(adminUserId)
            .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));
    }
}
```

- [ ] **Step 6: AdminController 뼈대 + getPendingUsers 구현**

```java
// src/main/java/com/example/community/domain/admin/controller/AdminController.java
package com.example.community.domain.admin.controller;

import com.example.community.common.dto.ApiResponse;
import com.example.community.domain.admin.dto.response.PendingUserResponse;
import com.example.community.domain.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
```

- [ ] **Step 7: 테스트 통과 확인**

```bash
./gradlew test --tests "com.example.community.domain.admin.*" 2>&1 | tail -10
```
예상: BUILD SUCCESSFUL, 3 tests passed (서비스 1 + 컨트롤러 2)

- [ ] **Step 8: 커밋**

```bash
git add src/main/java/com/example/community/domain/admin src/test/java/com/example/community/domain/admin
git commit -m "feat: 가입 대기 회원 조회 API 구현 (GET /api/admin/users/pending)"
```

---

### Task 5: 기능2 — 회원 상세 조회

> **Spring 개념:** `@PathVariable`은 URL의 `{userId}` 부분을 메서드 파라미터로 받는다. Service에서 `findById()` 결과가 없으면 `CustomException`을 던져 GlobalExceptionHandler가 404를 응답한다.

**Files:**
- Create: `src/main/java/com/example/community/domain/admin/dto/response/UserDetailResponse.java`
- Modify: `src/main/java/com/example/community/domain/admin/service/AdminService.java`
- Modify: `src/main/java/com/example/community/domain/admin/controller/AdminController.java`
- Test: `AdminServiceTest.java` (테스트 추가)
- Test: `AdminControllerTest.java` (테스트 추가)

- [ ] **Step 1: AdminServiceTest에 테스트 추가**

`AdminServiceTest` 클래스 안에 다음 테스트를 추가한다:

```java
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
```

`AdminServiceTest` import 맨 위에 추가:
```java
import com.example.community.domain.admin.dto.response.UserDetailResponse;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
```

- [ ] **Step 2: AdminControllerTest에 테스트 추가**

```java
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
```

`AdminControllerTest` import 맨 위에 추가:
```java
import com.example.community.domain.admin.dto.response.UserDetailResponse;
```

- [ ] **Step 3: UserDetailResponse 생성**

```java
// src/main/java/com/example/community/domain/admin/dto/response/UserDetailResponse.java
package com.example.community.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserDetailResponse {
    private Long userId;
    private Long studentId;
    private String nickname;
    private String name;
    private String school;
    private String status;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private Long postCount;
    private Long commentCount;
}
```

- [ ] **Step 4: AdminService에 getUserDetail 추가**

`AdminService` 클래스 안에 추가:

```java
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
```

`AdminService` import에 추가:
```java
import com.example.community.domain.admin.dto.response.UserDetailResponse;
import com.example.community.domain.post.enums.CommentStatus;
import com.example.community.domain.post.enums.PostStatus;
import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.repository.UserRepository;
```

- [ ] **Step 5: AdminController에 getUserDetail 추가**

`AdminController` 클래스 안에 추가:

```java
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetail(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserDetail(userId)));
    }
```

`AdminController` import에 추가:
```java
import com.example.community.domain.admin.dto.response.UserDetailResponse;
```

- [ ] **Step 6: 테스트 통과 확인**

```bash
./gradlew test --tests "com.example.community.domain.admin.*" 2>&1 | tail -10
```
예상: BUILD SUCCESSFUL

- [ ] **Step 7: 커밋**

```bash
git add src/main/java/com/example/community/domain/admin src/test/java/com/example/community/domain/admin
git commit -m "feat: 회원 상세 조회 API 구현 (GET /api/admin/users/{userId})"
```

---

### Task 6: 기능3 — 가입 승인

> **Spring 개념:** `@Transactional`이 붙은 메서드 안에서 엔티티를 수정하면, 메서드가 끝날 때 JPA가 변경사항을 자동으로 DB에 반영한다(더티 체킹). `save()`를 따로 호출할 필요 없다.

**Files:**
- Create: `src/main/java/com/example/community/domain/admin/dto/response/ApproveResponse.java`
- Modify: `AdminService.java`, `AdminController.java`
- Test: `AdminServiceTest.java`, `AdminControllerTest.java`

- [ ] **Step 1: AdminServiceTest에 테스트 추가**

```java
    @Test
    void approveUser_changesStatusToApproved() {
        User user = User.create(20201234L, "pw", "홍길동", "경북대학교", "닉네임1", "http://cert.url");
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

        ApproveResponse result = adminService.approveUser(1L);

        assertThat(result.getStatus()).isEqualTo("APPROVED");
        assertThat(result.getApprovedAt()).isNotNull();
    }
```

import 추가: `import com.example.community.domain.admin.dto.response.ApproveResponse;`

- [ ] **Step 2: AdminControllerTest에 테스트 추가**

```java
    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void approveUser_returns200() throws Exception {
        ApproveResponse response = new ApproveResponse(1L, "APPROVED", java.time.LocalDateTime.now());
        when(adminService.approveUser(1L)).thenReturn(response);

        mockMvc.perform(patch("/api/admin/users/1/approve"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }
```

import 추가: `import com.example.community.domain.admin.dto.response.ApproveResponse;`

- [ ] **Step 3: ApproveResponse 생성**

```java
// src/main/java/com/example/community/domain/admin/dto/response/ApproveResponse.java
package com.example.community.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApproveResponse {
    private Long userId;
    private String status;
    private LocalDateTime approvedAt;
}
```

- [ ] **Step 4: AdminService에 approveUser 추가**

```java
    @Transactional
    public ApproveResponse approveUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.approve();
        return new ApproveResponse(user.getId(), user.getStatus().name(), user.getApprovedAt());
    }
```

import 추가: `import com.example.community.domain.admin.dto.response.ApproveResponse;`

- [ ] **Step 5: AdminController에 approveUser 추가**

```java
    @PatchMapping("/users/{userId}/approve")
    public ResponseEntity<ApiResponse<ApproveResponse>> approveUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.approveUser(userId)));
    }
```

import 추가: `import com.example.community.domain.admin.dto.response.ApproveResponse;`

- [ ] **Step 6: 테스트 통과 확인**

```bash
./gradlew test --tests "com.example.community.domain.admin.*" 2>&1 | tail -10
```

- [ ] **Step 7: 커밋**

```bash
git add src/main/java/com/example/community/domain/admin src/test/java/com/example/community/domain/admin
git commit -m "feat: 가입 승인 API 구현 (PATCH /api/admin/users/{userId}/approve)"
```

---

### Task 7: 기능4 — 가입 반려

> **Spring 개념:** `@RequestBody`는 HTTP 요청 바디의 JSON을 Java 객체로 자동 변환한다. `@Valid`와 함께 쓰면 `@NotBlank` 같은 검증 어노테이션이 동작한다.

**Files:**
- Create: `RejectRequest.java`, `RejectResponse.java`
- Modify: `AdminService.java`, `AdminController.java`
- Test: `AdminServiceTest.java`, `AdminControllerTest.java`

- [ ] **Step 1: AdminServiceTest에 테스트 추가**

SecurityUtil은 `SecurityContextHolder`를 읽는다. Mockito로는 static 메서드를 mock할 수 없으므로, 테스트에서 직접 SecurityContext를 설정한다.

`AdminServiceTest` 클래스에 `@AfterEach` 추가 (처음 한 번만):
```java
    @AfterEach
    void clearSecurityContext() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }
```

그 다음 테스트 추가:
```java
    @Test
    void rejectUser_changesStatusAndSavesRejection() {
        // SecurityContext에 관리자(userId=99) 설정
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
```

import 추가:
```java
import com.example.community.domain.admin.dto.response.RejectResponse;
import com.example.community.domain.user.enums.AdminLevel;
```

- [ ] **Step 2: AdminControllerTest에 테스트 추가**

```java
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
```

import 추가:
```java
import com.example.community.domain.admin.dto.response.RejectResponse;
import static org.mockito.ArgumentMatchers.*;
```

- [ ] **Step 3: RejectRequest 생성**

```java
// src/main/java/com/example/community/domain/admin/dto/request/RejectRequest.java
package com.example.community.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RejectRequest {
    @NotBlank(message = "반려 사유를 입력해주세요.")
    private String reason;
}
```

- [ ] **Step 4: RejectResponse 생성**

```java
// src/main/java/com/example/community/domain/admin/dto/response/RejectResponse.java
package com.example.community.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RejectResponse {
    private Long userId;
    private String status;
}
```

- [ ] **Step 5: AdminService에 rejectUser 추가**

```java
    @Transactional
    public RejectResponse rejectUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Admin admin = findCurrentAdmin();
        user.reject();
        userRejectionRepository.save(UserRejection.of(user, admin, reason));
        return new RejectResponse(user.getId(), user.getStatus().name());
    }
```

import 추가:
```java
import com.example.community.domain.admin.dto.response.RejectResponse;
import com.example.community.domain.user.entity.UserRejection;
```

- [ ] **Step 6: AdminController에 rejectUser 추가**

```java
    @PatchMapping("/users/{userId}/reject")
    public ResponseEntity<ApiResponse<RejectResponse>> rejectUser(
        @PathVariable Long userId,
        @RequestBody @Valid RejectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.rejectUser(userId, request.getReason())));
    }
```

import 추가:
```java
import com.example.community.domain.admin.dto.request.RejectRequest;
import com.example.community.domain.admin.dto.response.RejectResponse;
import jakarta.validation.Valid;
```

- [ ] **Step 7: 테스트 통과 확인**

```bash
./gradlew test --tests "com.example.community.domain.admin.*" 2>&1 | tail -10
```

- [ ] **Step 8: 커밋**

```bash
git add src/main/java/com/example/community/domain/admin src/test/java/com/example/community/domain/admin
git commit -m "feat: 가입 반려 API 구현 (PATCH /api/admin/users/{userId}/reject)"
```

---

### Task 8: 기능5, 6 — 게시글/댓글 삭제 (소프트 삭제)

> **Spring 개념:** 소프트 삭제(soft delete)는 DB에서 행을 지우는 대신 `status=DELETED`, `deleted_at=현재시각`으로 표시하는 패턴이다. 데이터가 유지되어 복구나 감사(audit)가 가능하다.

**Files:**
- Modify: `AdminService.java`, `AdminController.java`
- Test: `AdminServiceTest.java`, `AdminControllerTest.java`

- [ ] **Step 1: AdminServiceTest에 테스트 2개 추가**

```java
    @Test
    void deletePost_softDeletesPost() {
        com.example.community.domain.post.entity.Post post =
            com.example.community.domain.post.entity.Post.createForTest(pendingUser, "제목", "내용");
        when(postRepository.findById(1L)).thenReturn(java.util.Optional.of(post));

        adminService.deletePost(1L);

        assertThat(post.getStatus()).isEqualTo(com.example.community.domain.post.enums.PostStatus.DELETED);
        assertThat(post.getDeletedAt()).isNotNull();
    }

    @Test
    void deleteComment_softDeletesComment() {
        com.example.community.domain.post.entity.Post post =
            com.example.community.domain.post.entity.Post.createForTest(pendingUser, "제목", "내용");
        com.example.community.domain.post.entity.Comment comment =
            com.example.community.domain.post.entity.Comment.createForTest(post, pendingUser, "댓글내용");
        when(commentRepository.findById(1L)).thenReturn(java.util.Optional.of(comment));

        adminService.deleteComment(1L);

        assertThat(comment.getStatus()).isEqualTo(com.example.community.domain.post.enums.CommentStatus.DELETED);
    }
```

- [ ] **Step 2: AdminControllerTest에 테스트 2개 추가**

```java
    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void deletePost_returns204() throws Exception {
        mockMvc.perform(delete("/api/admin/posts/1"))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void deleteComment_returns204() throws Exception {
        mockMvc.perform(delete("/api/admin/comments/1"))
            .andExpect(status().isNoContent());
    }
```

- [ ] **Step 3: Post.java에 createForTest 추가** (테스트 전용 팩토리)

```java
    public static Post createForTest(User user, String title, String content) {
        Post post = new Post();
        post.user = user;
        post.title = title;
        post.content = content;
        return post;
    }
```

- [ ] **Step 4: Comment.java에 createForTest 추가**

```java
    public static Comment createForTest(Post post, User user, String content) {
        Comment comment = new Comment();
        comment.post = post;
        comment.user = user;
        comment.content = content;
        return comment;
    }
```

- [ ] **Step 5: AdminService에 deletePost, deleteComment 추가**

```java
    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        post.softDelete();
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        comment.softDelete();
    }
```

import 추가:
```java
import com.example.community.domain.post.entity.Comment;
import com.example.community.domain.post.entity.Post;
```

- [ ] **Step 6: AdminController에 deletePost, deleteComment 추가**

```java
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
```

- [ ] **Step 7: 테스트 통과 확인**

```bash
./gradlew test --tests "com.example.community.domain.admin.*" 2>&1 | tail -10
```

- [ ] **Step 8: 커밋**

```bash
git add src/main/java/com/example/community/domain/admin src/main/java/com/example/community/domain/post/entity src/test/java/com/example/community/domain/admin
git commit -m "feat: 게시글/댓글 소프트 삭제 API 구현 (DELETE /api/admin/posts,comments)"
```

---

### Task 9: 기능7 — 회원 정지

> **Spring 개념:** 하나의 Service 메서드에서 여러 테이블을 수정해도 `@Transactional` 하나로 묶여 있으면 하나라도 실패할 때 모두 롤백된다.

**Files:**
- Create: `BanRequest.java`, `BanResponse.java`
- Modify: `AdminService.java`, `AdminController.java`
- Test: `AdminServiceTest.java`, `AdminControllerTest.java`

- [ ] **Step 1: AdminServiceTest에 테스트 추가**

```java
    @Test
    void banUser_changesStatusAndSavesSanction() {
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

        BanResponse result = adminService.banUser(1L, "커뮤니티 규정 위반");

        assertThat(result.getStatus()).isEqualTo("BANNED");
        assertThat(result.getStartAt()).isNotNull();
        assertThat(result.getEndAt()).isEqualTo(result.getStartAt().plusDays(7));
    }
```

import 추가: `import com.example.community.domain.admin.dto.response.BanResponse;`

- [ ] **Step 2: AdminControllerTest에 테스트 추가**

```java
    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void banUser_returns200() throws Exception {
        java.time.LocalDateTime startAt = java.time.LocalDateTime.now();
        BanResponse response = new BanResponse(1L, "BANNED", startAt, startAt.plusDays(7));
        when(adminService.banUser(eq(1L), anyString())).thenReturn(response);

        mockMvc.perform(patch("/api/admin/users/1/ban")
                .contentType("application/json")
                .content("{\"reason\":\"규정 위반\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("BANNED"));
    }
```

import 추가: `import com.example.community.domain.admin.dto.response.BanResponse;`

- [ ] **Step 3: BanRequest 생성**

```java
// src/main/java/com/example/community/domain/admin/dto/request/BanRequest.java
package com.example.community.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BanRequest {
    @NotBlank(message = "정지 사유를 입력해주세요.")
    private String reason;
}
```

- [ ] **Step 4: BanResponse 생성**

```java
// src/main/java/com/example/community/domain/admin/dto/response/BanResponse.java
package com.example.community.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BanResponse {
    private Long userId;
    private String status;
    private LocalDateTime startAt;
    private LocalDateTime endAt; // startAt + 7일 고정
}
```

- [ ] **Step 5: AdminService에 banUser 추가**

```java
    @Transactional
    public BanResponse banUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Admin admin = findCurrentAdmin();
        LocalDateTime startAt = LocalDateTime.now();
        LocalDateTime endAt = startAt.plusDays(7);
        user.ban();
        userSanctionRepository.save(UserSanction.of(user, admin, SanctionType.BAN, reason, startAt, endAt));
        return new BanResponse(user.getId(), user.getStatus().name(), startAt, endAt);
    }
```

import 추가:
```java
import com.example.community.domain.admin.dto.response.BanResponse;
import com.example.community.domain.user.entity.UserSanction;
import com.example.community.domain.user.enums.SanctionType;
import java.time.LocalDateTime;
```

- [ ] **Step 6: AdminController에 banUser 추가**

```java
    @PatchMapping("/users/{userId}/ban")
    public ResponseEntity<ApiResponse<BanResponse>> banUser(
        @PathVariable Long userId,
        @RequestBody @Valid BanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminService.banUser(userId, request.getReason())));
    }
```

import 추가:
```java
import com.example.community.domain.admin.dto.request.BanRequest;
import com.example.community.domain.admin.dto.response.BanResponse;
```

- [ ] **Step 7: 테스트 통과 확인**

```bash
./gradlew test --tests "com.example.community.domain.admin.*" 2>&1 | tail -10
```

- [ ] **Step 8: 커밋**

```bash
git add src/main/java/com/example/community/domain/admin src/test/java/com/example/community/domain/admin
git commit -m "feat: 회원 정지 API 구현 (PATCH /api/admin/users/{userId}/ban)"
```

---

### Task 10: 기능8 — 강제 탈퇴

**Files:**
- Create: `WithdrawRequest.java`
- Modify: `AdminService.java`, `AdminController.java`
- Test: `AdminServiceTest.java`, `AdminControllerTest.java`

- [ ] **Step 1: AdminServiceTest에 테스트 추가**

```java
    @Test
    void withdrawUser_changesStatusAndSavesSanction() {
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

        adminService.withdrawUser(1L, "운영 정책 위반");

        assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
    }
```

- [ ] **Step 2: AdminControllerTest에 테스트 추가**

```java
    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void withdrawUser_returns204() throws Exception {
        mockMvc.perform(delete("/api/admin/users/1")
                .contentType("application/json")
                .content("{\"reason\":\"운영 정책 위반\"}"))
            .andExpect(status().isNoContent());
    }
```

- [ ] **Step 3: WithdrawRequest 생성**

```java
// src/main/java/com/example/community/domain/admin/dto/request/WithdrawRequest.java
package com.example.community.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WithdrawRequest {
    @NotBlank(message = "탈퇴 사유를 입력해주세요.")
    private String reason;
}
```

- [ ] **Step 4: AdminService에 withdrawUser 추가**

```java
    @Transactional
    public void withdrawUser(Long userId, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Admin admin = findCurrentAdmin();
        LocalDateTime now = LocalDateTime.now();
        user.ban();
        userSanctionRepository.save(UserSanction.of(user, admin, SanctionType.FORCE_WITHDRAW, reason, now, null));
    }
```

- [ ] **Step 5: AdminController에 withdrawUser 추가**

```java
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> withdrawUser(
        @PathVariable Long userId,
        @RequestBody @Valid WithdrawRequest request) {
        adminService.withdrawUser(userId, request.getReason());
        return ResponseEntity.noContent().build();
    }
```

import 추가:
```java
import com.example.community.domain.admin.dto.request.WithdrawRequest;
```

- [ ] **Step 6: 전체 테스트 통과 확인**

```bash
./gradlew test 2>&1 | tail -15
```
예상: BUILD SUCCESSFUL, 모든 테스트 통과

- [ ] **Step 7: 최종 커밋**

```bash
git add src/main/java/com/example/community/domain/admin src/test/java/com/example/community/domain/admin
git commit -m "feat: 강제 탈퇴 API 구현 (DELETE /api/admin/users/{userId}) — Admin 기능 8개 완성"
```

---

## 완성 후 Swagger 확인

```bash
./gradlew bootRun
```

브라우저에서 `http://localhost:8080/swagger-ui/index.html` 접속 → Admin 엔드포인트 8개 확인
