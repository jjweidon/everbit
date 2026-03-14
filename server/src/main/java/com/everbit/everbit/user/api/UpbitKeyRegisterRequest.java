package com.everbit.everbit.user.api;

import jakarta.validation.constraints.NotBlank;

/**
 * POST /api/v2/upbit/key 요청. 로그/응답에 절대 노출하지 않는다.
 */
public record UpbitKeyRegisterRequest(
	@NotBlank(message = "accessKey is required")
	String accessKey,
	@NotBlank(message = "secretKey is required")
	String secretKey
) {}
