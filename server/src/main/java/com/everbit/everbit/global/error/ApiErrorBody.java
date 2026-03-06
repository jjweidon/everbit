package com.everbit.everbit.global.error;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 표준 API 에러 응답 본문.
 * SoT: docs/api/contracts.md §9, §11.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorBody(
	String code,
	String message,
	String reasonCode,
	java.util.Map<String, Object> details
) {
	public static ApiErrorBody of(String code, String message, String reasonCode) {
		return new ApiErrorBody(code, message, reasonCode, null);
	}

	public static ApiErrorBody of(String code, String message, String reasonCode, java.util.Map<String, Object> details) {
		return new ApiErrorBody(code, message, reasonCode, details);
	}
}
