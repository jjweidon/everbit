package com.everbit.everbit.global.exception;

import com.everbit.everbit.global.dto.ApiResponse;
import com.everbit.everbit.upbit.exception.UpbitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UpbitException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleUpbitException(UpbitException e) {
        log.error("UpbitException occurred", e);
        return ApiResponse.error(e.getMessage());
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleHttpStatusCodeException(HttpStatusCodeException e) {
        log.error("HttpStatusCodeException occurred - Status: {}, Response: {}", 
            e.getStatusCode(), e.getResponseBodyAsString());
        return ApiResponse.error("업비트 API 호출 실패: " + e.getResponseBodyAsString());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException occurred", e);
        return ApiResponse.error(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("Unexpected error occurred", e);
        return ApiResponse.error("서버 내부 오류가 발생했습니다: " + e.getMessage());
    }
}