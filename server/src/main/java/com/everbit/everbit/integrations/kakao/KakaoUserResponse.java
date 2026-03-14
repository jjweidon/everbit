package com.everbit.everbit.integrations.kakao;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * 카카오 /v2/user/me 응답.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoUserResponse(
	long id,
	KakaoAccount kakaoAccount  // nullable when email not agreed
) {
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public record KakaoAccount(
		String email,
		Boolean isEmailValid,
		Boolean isEmailVerified
	) {}
}
