package com.everbit.everbit.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * OAuth2 공통 설정. SoT: spring.security.oauth2.*
 */
@Validated
@ConfigurationProperties(prefix = "spring.security.oauth2")
public record OAuth2Properties(
	String authenticatedRedirectUri,
	String logoutRedirectUri,
	String allowedOrigins  // comma-separated, e.g. "http://localhost:3000,https://everbit.kr"
) {
	public String[] getAllowedOriginsArray() {
		return allowedOrigins == null ? new String[0] : allowedOrigins.split(",\\s*");
	}
}
