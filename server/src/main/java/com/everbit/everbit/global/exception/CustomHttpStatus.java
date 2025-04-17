package com.everbit.everbit.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CustomHttpStatus {
    OK(200, "요청이 성공했습니다."),
    CREATED(201, "리소스가 성공적으로 생성되었습니다."),
    NO_CONTENT(204, "반환할 데이터가 없습니다."),
    BAD_REQUEST(400, "잘못된 요청입니다."),
    UNAUTHORIZED(401, "인증이 필요합니다."),
    FORBIDDEN(403, "접근이 금지되었습니다."),
    NOT_FOUND(404, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다.");

    private final int value;
    private final String message;

    public HttpStatus toHttpStatus() {
        return HttpStatus.valueOf(this.value);
    }
}
