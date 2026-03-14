package com.everbit.everbit.auth.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * JWT 설정. SoT: spring.jwt.*
 */
@Validated
@ConfigurationProperties(prefix = "spring.jwt")
public record JwtProperties(
	@NotBlank String accessSecret,
	@NotBlank String refreshSecret,
	int accessTtlSeconds,
	int refreshTtlSeconds
) {
}
