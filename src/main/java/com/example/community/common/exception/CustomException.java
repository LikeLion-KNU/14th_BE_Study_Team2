package com.example.community.common.exception;

import lombok.Getter;

/**
 * [커스텀 예외]
 *
 * RuntimeException을 상속하는 이유:
 * → Checked Exception(IOException 등)은 throws 선언과 try-catch를 강제 → 코드가 지저분해짐
 * → RuntimeException(Unchecked)은 선언 없이 던질 수 있고, GlobalExceptionHandler가 일괄 처리
 *
 * ErrorCode를 들고 다니는 이유:
 * → GlobalExceptionHandler에서 어떤 HTTP 상태 코드로 응답할지 결정하기 위해
 * → throw new CustomException(ErrorCode.USER_NOT_FOUND) 한 줄로 표준 에러 응답 생성
 */
@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // RuntimeException의 message 필드에 에러 메시지 저장
        this.errorCode = errorCode;
    }
}
