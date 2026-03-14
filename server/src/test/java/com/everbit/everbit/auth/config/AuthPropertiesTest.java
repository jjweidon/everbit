package com.everbit.everbit.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AuthProperties 속성 바인딩·부팅 실패 검증.
 * SoT: docs/testing/tdd.md §4.8, docs/testing/backend-tdd-template.md §10.
 * 빈 wiring이 아니라 속성 바인딩만 검증.
 */
class AuthPropertiesTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withUserConfiguration(EnableAuthPropertiesConfig.class)
		.withPropertyValues(
			"auth.jwt-access-secret=test-access-secret-min-32-chars-required",
			"auth.jwt-refresh-secret=test-refresh-secret-min-32-chars-required",
			"auth.jwt-access-ttl-seconds=900",
			"auth.jwt-refresh-ttl-seconds=1209600",
			"auth.allowed-origins=http://localhost:3000,http://127.0.0.1:3000"
		);

	@Test
	void auth_properties_정상_바인딩() {
		contextRunner.run(context -> {
			assertThat(context).hasNotFailed();
			AuthProperties props = context.getBean(AuthProperties.class);
			assertThat(props.jwtAccessTtlSeconds()).isEqualTo(900);
			assertThat(props.jwtRefreshTtlSeconds()).isEqualTo(1209600);
			assertThat(props.getAllowedOriginsArray()).contains("http://localhost:3000", "http://127.0.0.1:3000");
		});
	}

	@Test
	void 필수값_없으면_부팅_실패() {
		new ApplicationContextRunner()
			.withUserConfiguration(EnableAuthPropertiesConfig.class)
			.withPropertyValues(
				"auth.jwt-access-ttl-seconds=900",
				"auth.jwt-refresh-ttl-seconds=1209600"
			)
			.run(context -> assertThat(context).hasFailed());
	}

	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(AuthProperties.class)
	static class EnableAuthPropertiesConfig {
	}
}
