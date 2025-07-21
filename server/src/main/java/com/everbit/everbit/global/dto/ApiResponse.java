package com.everbit.everbit.global.dto;

import com.everbit.everbit.global.exception.CustomHttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiResponse<T> extends ResponseEntity<ApiResponse.Body<T>> {

    private ApiResponse(Body<T> body, CustomHttpStatus status) {
        super(body, status.toHttpStatus());
    }

    // 성공 응답 (200 OK)
    public static <T> ApiResponse<T> success(T data, String message) {
        Body<T> body = new Body<>(200, message, data);
        return new ApiResponse<>(body, CustomHttpStatus.OK);
    }

    // 생성 성공 응답 (201 Created)
    public static <T> ApiResponse<T> created(T data, String message) {
        Body<T> body = new Body<>(201, message, data);
        return new ApiResponse<>(body, CustomHttpStatus.CREATED);
    }

    // 삭제 성공 응답 (204 No Content)
    public static <T> ApiResponse<T> noContent(String message) {
        Body<T> body = new Body<>(204, message, null);
        return new ApiResponse<>(body, CustomHttpStatus.NO_CONTENT);
    }

    // 실패 응답 (400 Bad Request)
    public static <T> ApiResponse<T> failure(String message) {
        return failure(message, CustomHttpStatus.BAD_REQUEST);
    }

    // 실패 응답 (특정 상태 코드)
    public static <T> ApiResponse<T> failure(String message, CustomHttpStatus status) {
        Body<T> body = new Body<>(status.getValue(), message, null);
        return new ApiResponse<>(body, status);
    }

    public static <T> ApiResponse<T> of(CustomHttpStatus status, T data) {
        Body<T> body = new Body<>(status.getValue(), status.getMessage(), data);
        return new ApiResponse<>(body, status);
    }

    public static <T> ApiResponse<T> of(CustomHttpStatus status) {
        return of(status, null);
    }

    public record Body<T>(int code, String message, T data) {
        // 응답 코드가 200번대인지 확인하는 유틸리티 메서드
        public boolean isSuccess() {
            return code >= 200 && code < 300;
        }
    }
}