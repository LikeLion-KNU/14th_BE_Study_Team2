package com.example.community.api.user.exception;

import com.example.community.api.user.dto.AuthErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<AuthErrorResponse> handleAuthException(
            AuthException e,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(e.getStatus())
                .body(AuthErrorResponse.of(
                        e.getStatus(),
                        e.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthErrorResponse> handleValidationException(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + " 값이 올바르지 않습니다.")
                .orElse("요청 값이 올바르지 않습니다.");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(AuthErrorResponse.of(
                        HttpStatus.BAD_REQUEST,
                        message,
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<AuthErrorResponse> handleInvalidJsonException(
            HttpMessageNotReadableException e,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(AuthErrorResponse.of(
                        HttpStatus.BAD_REQUEST,
                        "요청 본문 형식이 올바르지 않습니다.",
                        request.getRequestURI()
                ));
    }
}