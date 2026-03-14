package com.everbit.everbit.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth2 로그인 실패 시 프론트 로그인 페이지로 리다이렉트.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	private final AuthProperties authProperties;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException {
		log.warn("OAuth2 login failure: {}", exception.getMessage());
		String message = "카카오 로그인에 실패했습니다.";
		String url = authProperties.frontendBaseUrl() + "/login?error="
			+ URLEncoder.encode(message, StandardCharsets.UTF_8);
		response.sendRedirect(url);
	}
}
