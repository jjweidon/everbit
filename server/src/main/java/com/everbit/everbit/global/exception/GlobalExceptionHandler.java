package com.everbit.everbit.global.exception;

import com.everbit.everbit.global.dto.ApiResponse;
import com.everbit.everbit.user.exception.UserException;

import org.apache.kafka.common.errors.AuthorizationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalExceptionHandler {

    // UserNotFoundException 처리
    @ExceptionHandler(UserException.class)
    public ApiResponse<Object> handleUserNotFoundException(UserException ex) {
        return ApiResponse.of(CustomHttpStatus.NOT_FOUND);
    }

    // IllegalStateException 처리
    @ExceptionHandler(IllegalStateException.class)
    public ApiResponse<Object> handleIllegalStateException(IllegalStateException ex) {
        return ApiResponse.of(CustomHttpStatus.BAD_REQUEST);
    }

    // 기타 모든 예외 처리 (500 서버 에러)
    @ExceptionHandler(Exception.class)
    public ApiResponse<Object> handleException(Exception ex) {
        return ApiResponse.of(CustomHttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ApiResponse<Object> handleAuthenticationException(AuthenticationException ex) {
        return ApiResponse.of(CustomHttpStatus.UNAUTHENTICATED);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ApiResponse<Object> handleAuthorizationException(AuthorizationException ex) {
        return ApiResponse.of(CustomHttpStatus.FORBIDDEN);
    }
}