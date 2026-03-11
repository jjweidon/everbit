package com.everbit.everbit.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 카카오 OAuth2 공급자 설정. SoT: spring.security.oauth2.client.provider.kakao.*
 */
@Validated
@ConfigurationProperties(prefix = "spring.security.oauth2.client.provider.kakao")
public record KakaoProviderProperties(
	String authorizationUri,
	String tokenUri,
	String userInfoUri,
	String userNameAttribute
) {
}
