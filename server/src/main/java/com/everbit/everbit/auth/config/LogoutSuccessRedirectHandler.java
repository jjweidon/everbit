package com.everbit.everbit.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * /logout 성공 후 프론트 로그인 페이지로 리다이렉트.
 */
@Component
@RequiredArgsConstructor
public class LogoutSuccessRedirectHandler extends SimpleUrlLogoutSuccessHandler {

	private final AuthProperties authProperties;

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		String targetUrl = authProperties.frontendBaseUrl() + "/login";
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
