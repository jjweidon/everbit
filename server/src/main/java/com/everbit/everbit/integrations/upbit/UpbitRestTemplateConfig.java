package com.everbit.everbit.integrations.upbit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Upbit 전용 RestTemplate. SoT: docs/integrations/upbit.md §5 (timeout 3~5s).
 */
@Configuration
@EnableConfigurationProperties(UpbitProperties.class)
public class UpbitRestTemplateConfig {

	@Bean
	public RestTemplate upbitRestTemplate(UpbitProperties properties) {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(properties.connectTimeoutSeconds()));
		factory.setReadTimeout(Duration.ofSeconds(properties.readTimeoutSeconds()));
		return new RestTemplate(factory);
	}
}
