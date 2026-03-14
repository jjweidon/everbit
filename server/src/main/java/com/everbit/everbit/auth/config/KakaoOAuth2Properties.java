package com.everbit.everbit.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 카카오 OAuth2 클라이언트 설정. SoT: spring.security.oauth2.client.registration.kakao.*
 */
@Validated
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.kakao")
public record KakaoOAuth2Properties(
	String clientId,
	String clientSecret,
	String redirectUri,
	String scope
) {
}
