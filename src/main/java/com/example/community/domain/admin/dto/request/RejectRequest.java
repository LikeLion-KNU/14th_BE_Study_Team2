package com.example.community.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * [Java record — DTO에 적합한 이유]
 *
 * record는 Java 16에서 정식 도입된 불변(immutable) 데이터 클래스.
 * 아래를 자동으로 생성:
 *   - 생성자: RejectRequest(String reason)
 *   - accessor: reason() — Lombok의 getReason()과 달리 get 접두사 없음
 *   - equals(), hashCode(), toString()
 *
 * @NotBlank: reason이 null이거나 빈 문자열(" ")이면 검증 실패 → 400 Bad Request
 * → Controller의 @Valid가 있어야 실제로 검증이 실행됨
 *
 * Jackson이 JSON → record 역직렬화:
 *   {"reason": "서류 미비"} → new RejectRequest("서류 미비")
 */
public record RejectRequest(
    @NotBlank(message = "반려 사유를 입력해주세요.") String reason
) {}
