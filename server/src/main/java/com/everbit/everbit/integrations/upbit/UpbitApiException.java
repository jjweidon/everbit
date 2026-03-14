package com.everbit.everbit.integrations.upbit;

import org.springframework.http.HttpStatusCode;

/**
 * Upbit HTTP 응답 오류(4xx/5xx) 래핑.
 * 429 → THROTTLED, 418 → BLOCKED 등 Reason Code 매핑에 사용.
 */
public class UpbitApiException extends UpbitException {

	private final HttpStatusCode statusCode;
	private final String responseBody;

	public UpbitApiException(HttpStatusCode statusCode, String responseBody) {
		super("Upbit API error: " + statusCode + " - " + (responseBody != null ? maskBody(responseBody) : ""));
		this.statusCode = statusCode;
		this.responseBody = responseBody;
	}

	public HttpStatusCode getStatusCode() {
		return statusCode;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public boolean is429() {
		return statusCode != null && statusCode.value() == 429;
	}

	public boolean is418() {
		return statusCode != null && statusCode.value() == 418;
	}

	private static String maskBody(String body) {
		if (body == null || body.length() <= 80) {
			return "[redacted]";
		}
		return body.substring(0, 40) + "...[redacted]";
	}
}
