package com.everbit.everbit.global.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "upbit.key")
public record UpbitKeyCryptoProperties(
	String masterKey
) {
	public boolean isConfigured() {
		return masterKey != null && !masterKey.isBlank();
	}
}
