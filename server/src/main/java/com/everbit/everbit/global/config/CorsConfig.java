package com.everbit.everbit.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 기본 설정. SoT: docs/architecture/modular-monolith.md.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Value("${cors.allowed-origins:http://localhost:3000}")
	private String allowedOrigins;

	@Override
	@SuppressWarnings("null")
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOrigins(allowedOrigins.split(","))
			.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
			.allowedHeaders("*")
			.allowCredentials(true);
	}
}
