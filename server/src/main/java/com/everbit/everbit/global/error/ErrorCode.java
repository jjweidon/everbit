package com.everbit.everbit.global.error;

/**
 * 표준 에러 코드. SoT: docs/api/contracts.md §9, §11.
 */
public enum ErrorCode {
	INTERNAL_ERROR,
	UNAUTHORIZED,
	FORBIDDEN,
	RATE_LIMIT_EXCEEDED,
	SERVICE_UNAVAILABLE,
	ORDER_NOT_FOUND,
	BAD_REQUEST,
	;
}
