package com.example.community.common.exception;

import com.example.community.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * [전역 예외 처리기]
 *
 * @RestControllerAdvice: 모든 @RestController에서 던진 예외를 한 곳에서 처리
 * → 각 Controller/Service마다 try-catch를 쓸 필요 없음
 * → Spring AOP 기반: 예외가 터지면 해당 핸들러 메서드로 자동으로 흘러옴
 *
 * 흐름:
 *   Service에서 throw new CustomException(ErrorCode.USER_NOT_FOUND)
 *   → Spring이 handleCustomException() 호출
 *   → HTTP 404 + { "success": false, "errorCode": "USER_NOT_FOUND", ... } 응답
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @ExceptionHandler(CustomException.class):
     * CustomException 타입의 예외가 발생하면 이 메서드가 처리
     *
     * switch 표현식(Java 14+):
     * → ErrorCode에 새 케이스 추가 시 여기 switch에 추가 안 하면 컴파일 에러 → 실수 방지
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        HttpStatus status = switch (e.getErrorCode()) {
            case FORBIDDEN -> HttpStatus.FORBIDDEN;                                          // 403
            case USER_NOT_FOUND, POST_NOT_FOUND, COMMENT_NOT_FOUND, ADMIN_NOT_FOUND -> HttpStatus.NOT_FOUND; // 404
            case BAD_REQUEST -> HttpStatus.BAD_REQUEST;                                     // 400
        };
        return ResponseEntity.status(status).body(ApiResponse.error(e.getErrorCode()));
    }
}
