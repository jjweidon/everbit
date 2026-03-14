package com.everbit.everbit.global.error;

import com.everbit.everbit.auth.api.AuthController;
import com.everbit.everbit.user.application.NotOwnerException;
import com.everbit.everbit.user.application.OwnerNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 전역 API 예외 처리. 표준 에러 본문(code, message, reasonCode, details) 반환.
 * SoT: docs/api/contracts.md §9, §11.
 * 커스텀 예외는 도메인별로 정의하고 여기서 HTTP/Reason Code로만 매핑.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AuthController.InvalidOriginException.class)
	public ResponseEntity<ApiErrorBody> handleInvalidOrigin() {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
			.body(ApiErrorBody.of(ErrorCode.FORBIDDEN, "유효하지 않은 요청입니다.", "INVALID_ORIGIN"));
	}

	@ExceptionHandler(NotOwnerException.class)
	public ResponseEntity<ApiErrorBody> handleNotOwner(NotOwnerException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
			.body(ApiErrorBody.of(ErrorCode.FORBIDDEN, ex.getMessage(), "NOT_OWNER"));
	}

	@ExceptionHandler(OwnerNotFoundException.class)
	public ResponseEntity<ApiErrorBody> handleOwnerNotFound(OwnerNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ApiErrorBody.of(ErrorCode.NOT_FOUND, "요청한 계정을 찾을 수 없습니다.", "OWNER_NOT_FOUND"));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorBody> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
			.map(e -> e.getField() + ": " + e.getDefaultMessage())
			.collect(Collectors.joining("; "));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(ApiErrorBody.of(ErrorCode.BAD_REQUEST, message, "VALIDATION_FAILED"));
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
