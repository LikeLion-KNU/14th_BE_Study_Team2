package com.example.community.common.dto;

import com.example.community.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * [공통 API 응답 래퍼]
 *
 * 모든 API 응답을 아래 형태로 통일:
 *   성공: { "success": true,  "data": {...},  "message": "요청 성공" }
 *   실패: { "success": false, "errorCode": "USER_NOT_FOUND", "message": "존재하지 않는 사용자입니다." }
 *
 * 왜 래퍼를 씀?
 * → 프론트엔드가 success 필드 하나로 성공/실패를 일관되게 판단 가능
 * → 에러 코드와 메시지를 표준화된 위치에서 전달
 *
 * <T>: 제네릭. 성공 응답의 data 타입을 호출부에서 결정
 *   ex) ApiResponse<PendingUserResponse>, ApiResponse<Void>
 */
@Getter

/**
 * @JsonInclude(NON_NULL): null인 필드는 JSON에서 아예 제외
 *   성공 응답 → errorCode가 null → JSON에 errorCode 키 자체가 없음
 *   실패 응답 → data가 null → JSON에 data 키 자체가 없음
 * → 불필요한 null 필드가 응답에 노출되지 않음
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String errorCode;
    private final String message;

    // private 생성자: 외부에서 new ApiResponse()로 직접 생성 불가
    // 반드시 아래 정적 팩토리 메서드(success, error)를 통해서만 생성
    private ApiResponse(boolean success, T data, String errorCode, String message) {
        this.success = success;
        this.data = data;
        this.errorCode = errorCode;
        this.message = message;
    }

    // 성공 응답 생성: errorCode = null (JSON에서 제외됨)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, "요청 성공");
    }

    // 실패 응답 생성: data = null (JSON에서 제외됨)
    // ErrorCode enum의 name() → 문자열 "USER_NOT_FOUND"
    // ErrorCode enum의 getMessage() → "존재하지 않는 사용자입니다."
    public static ApiResponse<Void> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, errorCode.name(), errorCode.getMessage());
    }
}
