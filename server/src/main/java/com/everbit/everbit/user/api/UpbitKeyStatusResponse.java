package com.everbit.everbit.user.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * GET /api/v2/upbit/key/status 응답. SoT: docs/api/contracts.md, client UpbitKeyStatusResponse.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UpbitKeyStatusResponse(
	String status,
	Instant lastVerifiedAt,
	String verificationErrorCode
) {
	public static final String REGISTERED = "REGISTERED";
	public static final String NOT_REGISTERED = "NOT_REGISTERED";
	public static final String VERIFICATION_FAILED = "VERIFICATION_FAILED";
}
