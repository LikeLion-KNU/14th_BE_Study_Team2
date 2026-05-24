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
