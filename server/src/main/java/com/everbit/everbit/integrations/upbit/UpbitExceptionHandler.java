package com.everbit.everbit.integrations.upbit;

import com.everbit.everbit.global.error.ApiErrorBody;
import com.everbit.everbit.global.error.ErrorCode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Upbit 연동 도메인 예외 전용 핸들러.
 * 업비트 관련 예외 처리를 integrations/upbit 패키지에 응집하여 유지보수성을 높인다.
 * SoT: docs/api/contracts.md §11, docs/integrations/upbit.md §4.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UpbitExceptionHandler {

	@ExceptionHandler(UpbitApiException.class)
	public ResponseEntity<ApiErrorBody> handleUpbitApi(UpbitApiException ex) {
		if (ex.is429()) {
			return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
				.body(ApiErrorBody.of(ErrorCode.RATE_LIMIT_EXCEEDED, "Upbit 요청 제한 초과. 잠시 후 재시도해 주세요.", "RATE_LIMIT_429"));
		}
		if (ex.is418()) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(ApiErrorBody.of(ErrorCode.SERVICE_UNAVAILABLE, "Upbit API 접근이 일시적으로 제한되었습니다.", "UPBIT_BLOCKED_418"));
		}
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
			.body(ApiErrorBody.of(ErrorCode.UPBIT_ERROR, "Upbit API 오류가 발생했습니다.", "UPBIT_API_ERROR"));
	}

	@ExceptionHandler(UpbitException.class)
	public ResponseEntity<ApiErrorBody> handleUpbit(UpbitException ex) {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
			.body(ApiErrorBody.of(ErrorCode.UPBIT_ERROR, "Upbit 연동 오류가 발생했습니다.", "UPBIT_ERROR"));
	}
}
