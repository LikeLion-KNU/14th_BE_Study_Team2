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
