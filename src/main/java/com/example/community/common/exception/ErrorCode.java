package com.example.community.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * [에러 코드 enum]
 *
 * 에러 종류를 코드로 관리하는 이유:
 * → "USER_NOT_FOUND" 같은 문자열 상수로 검색 가능 → 어디서 던지고 어디서 처리하는지 추적 쉬움
 * → 에러 메시지를 한 곳에서 관리 → 수정 시 여러 파일 안 건드려도 됨
 * → switch 문에서 모든 케이스를 강제로 처리하게 컴파일러가 체크해줌 (타입 안전성)
 *
 * @RequiredArgsConstructor: final 필드(message)를 받는 생성자 자동 생성
 * → enum은 각 상수 선언 시 생성자를 호출: USER_NOT_FOUND("존재하지 않는 사용자입니다.")
 */
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
