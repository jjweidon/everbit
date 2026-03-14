package com.everbit.everbit.integrations.kakao;

public interface KakaoTokenClient {
	KakaoTokenResponse exchangeToken(String code);
	KakaoUserResponse getUserInfo(String accessToken);
}
