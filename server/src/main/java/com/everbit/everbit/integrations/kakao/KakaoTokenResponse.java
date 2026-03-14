package com.everbit.everbit.integrations.kakao;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoTokenResponse(
	String accessToken,
	String tokenType,
	String refreshToken,
	int expiresIn,
	String scope
) {}
