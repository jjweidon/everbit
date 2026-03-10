package com.everbit.everbit.auth.config;

import org.springframework.stereotype.Component;

/**
 * 인증 설정 facade. JwtProperties, OAuth2Properties, KakaoOAuth2Properties를 통합.
 * SoT: docs/operations/environments.md §3.2.
 */
@Component
public record AuthProperties(
	JwtProperties jwt,
	OAuth2Properties oauth2,
	KakaoOAuth2Properties kakao
) {
	public String jwtAccessSecret() {
		return jwt.accessSecret();
	}

	public String jwtRefreshSecret() {
		return jwt.refreshSecret();
	}

	public int jwtAccessTtlSeconds() {
		return jwt.accessTtlSeconds();
	}

	public int jwtRefreshTtlSeconds() {
		return jwt.refreshTtlSeconds();
	}

	public String kakaoClientId() {
		return kakao.clientId();
	}

	public String kakaoClientSecret() {
		return kakao.clientSecret();
	}

	public String kakaoRedirectUri() {
		return kakao.redirectUri();
	}

	public String frontendBaseUrl() {
		return oauth2.authenticatedRedirectUri();
	}

	public String[] getAllowedOriginsArray() {
		return oauth2.getAllowedOriginsArray();
	}
}
