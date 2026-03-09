package com.everbit.everbit.global.error;

import com.everbit.everbit.auth.api.AuthController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 API 예외 처리. 표준 에러 본문(code, message, reasonCode, details) 반환.
 * SoT: docs/api/contracts.md §9, §11.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AuthController.InvalidOriginException.class)
	public ResponseEntity<ApiErrorBody> handleInvalidOrigin() {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
			.body(ApiErrorBody.of(ErrorCode.FORBIDDEN, "유효하지 않은 요청입니다.", "INVALID_ORIGIN"));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorBody> handleException(Exception ex) {
		ApiErrorBody body = ApiErrorBody.of(
			ErrorCode.INTERNAL_ERROR,
			"일시적인 오류가 발생했습니다.",
			"UNEXPECTED_ERROR"
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}
}
