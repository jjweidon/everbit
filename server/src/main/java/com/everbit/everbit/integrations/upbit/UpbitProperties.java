package com.everbit.everbit.integrations.upbit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Upbit API 설정. SoT: docs/integrations/upbit.md.
 * base-url만 설정, 키는 DB(UpbitKey)에서 암호문으로 보관.
 */
@ConfigurationProperties(prefix = "upbit.api")
public record UpbitProperties(
	String baseUrl,
	int connectTimeoutSeconds,
	int readTimeoutSeconds
) {
	public static final String DEFAULT_BASE_URL = "https://api.upbit.com";

	public UpbitProperties {
		if (baseUrl == null || baseUrl.isBlank()) {
			baseUrl = DEFAULT_BASE_URL;
		}
		if (connectTimeoutSeconds <= 0) {
			connectTimeoutSeconds = 3;
		}
		if (readTimeoutSeconds <= 0) {
			readTimeoutSeconds = 5;
		}
	}
}
