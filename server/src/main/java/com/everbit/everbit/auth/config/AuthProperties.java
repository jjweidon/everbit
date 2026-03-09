package com.everbit.everbit.auth.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 인증 설정. SoT: docs/operations/environments.md §3.2.
 */
@Validated
@ConfigurationProperties(prefix = "auth")
public record AuthProperties(
	@NotBlank String jwtAccessSecret,
	@NotBlank String jwtRefreshSecret,
	int jwtAccessTtlSeconds,
	int jwtRefreshTtlSeconds,
	String kakaoClientId,
	String kakaoClientSecret,
	String kakaoRedirectUri,
	String frontendBaseUrl,
	String allowedOrigins  // comma-separated, e.g. "http://localhost:3000,https://everbit.kr"
) {
	public String[] getAllowedOriginsArray() {
		return allowedOrigins == null ? new String[0] : allowedOrigins.split(",\\s*");
	}
}
