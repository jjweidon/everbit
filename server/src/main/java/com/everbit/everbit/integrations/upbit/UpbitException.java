package com.everbit.everbit.integrations.upbit;

/**
 * Upbit API 호출 실패 시 사용.
 * 429/418 등은 별도 Reason Code로 구분. SoT: docs/integrations/upbit.md, docs/api/contracts.md.
 */
public class UpbitException extends RuntimeException {

	public UpbitException(String message) {
		super(message);
	}

	public UpbitException(String message, Throwable cause) {
		super(message, cause);
	}
}
