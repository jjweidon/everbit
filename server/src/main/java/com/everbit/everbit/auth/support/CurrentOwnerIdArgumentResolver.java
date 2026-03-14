package com.everbit.everbit.auth.support;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link CurrentOwnerId}가 붙은 Long 파라미터에 JWT 인증된 ownerId를 주입.
 * SoT: JwtAuthenticationFilter가 설정한 principal(Long).
 */
@Component
public class CurrentOwnerIdArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(@NonNull MethodParameter parameter) {
		return parameter.getParameterAnnotation(CurrentOwnerId.class) != null
			&& Long.class.equals(parameter.getParameterType());
	}

	@Override
	@NonNull
	public Object resolveArgument(@NonNull MethodParameter parameter,
		@Nullable ModelAndViewContainer mavContainer,
		@NonNull NativeWebRequest webRequest,
		@Nullable WebDataBinderFactory binderFactory) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getPrincipal() == null) {
			throw new IllegalStateException("Expected authenticated principal (ownerId)");
		}
		Object principal = auth.getPrincipal();
		if (!(principal instanceof Long ownerId)) {
			throw new IllegalStateException("Expected Long principal (ownerId)");
		}
		return ownerId;
	}
}
