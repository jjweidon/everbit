package com.everbit.everbit.auth.config;

import com.everbit.everbit.auth.support.CurrentOwnerIdArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * JWT 기반 현재 사용자(ownerId) 주입을 위한 ArgumentResolver 등록.
 * 컨트롤러에서 @CurrentOwnerId Long ownerId 파라미터 사용 가능.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcAuthConfig implements WebMvcConfigurer {

	private final CurrentOwnerIdArgumentResolver currentOwnerIdArgumentResolver;

	@Override
	public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(0, currentOwnerIdArgumentResolver);
	}
}
