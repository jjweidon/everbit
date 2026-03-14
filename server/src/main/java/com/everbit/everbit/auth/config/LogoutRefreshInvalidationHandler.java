package com.everbit.everbit.auth.config;

import com.everbit.everbit.auth.application.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

/**
 * Spring Security 기본 /logout 호출 시 Refresh 쿠키 무효화 및 쿠키 삭제.
 * 로컬 서비스 로그아웃만 수행(카카오 계정 로그아웃 아님).
 */
@Component
@RequiredArgsConstructor
public class LogoutRefreshInvalidationHandler implements LogoutHandler {

	private static final String REFRESH_COOKIE_NAME = "everbit_refresh";

	private final AuthService authService;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		String refreshJti = getRefreshCookie(request);
		if (refreshJti != null) {
			authService.logout(refreshJti);
		}
		clearRefreshCookie(response);
	}

	private String getRefreshCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) return null;
		for (Cookie c : cookies) {
			if (REFRESH_COOKIE_NAME.equals(c.getName())) {
				return c.getValue();
			}
		}
		return null;
	}

	private void clearRefreshCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, "");
		cookie.setHttpOnly(true);
		cookie.setPath("/api/v2/auth");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
	}
}
