package com.everbit.everbit.global.error;

/**
 * 표준 에러 코드. SoT: docs/api/contracts.md §9, §11.
 * Upbit/키 관련: RATE_LIMIT_EXCEEDED(429), SERVICE_UNAVAILABLE(418·503), UPBIT_ERROR.
 */
public enum ErrorCode {
	INTERNAL_ERROR,
	UNAUTHORIZED,
	FORBIDDEN,
	NOT_FOUND,
	RATE_LIMIT_EXCEEDED,
	SERVICE_UNAVAILABLE,
	ORDER_NOT_FOUND,
	BAD_REQUEST,
	/** Upbit API 오류(4xx/5xx·타임아웃). 429/418은 별도 매핑. */
	UPBIT_ERROR,
	;
}
