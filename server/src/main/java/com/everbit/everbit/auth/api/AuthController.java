package com.everbit.everbit.auth.api;

import com.everbit.everbit.auth.application.AuthService;
import com.everbit.everbit.auth.config.AuthProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * 인증 API. Refresh만 제공. 로그인은 /oauth2/authorization/kakao, 로그아웃은 /logout.
 * SoT: docs/api/contracts.md, docs/integrations/kakao-oauth-auth-flow.md.
 */
@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController {

	private static final String REFRESH_COOKIE_NAME = "everbit_refresh";
	private static final int COOKIE_MAX_AGE_14_DAYS = 14 * 24 * 60 * 60;

	private final AuthService authService;
	private final AuthProperties authProperties;

	@PostMapping("/refresh")
	public ResponseEntity<RefreshResponse> refresh(
		HttpServletRequest request,
		HttpServletResponse response
	) {
		validateOrigin(request);

		String refreshJti = getRefreshCookie(request);
		if (refreshJti == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(null);
		}

		try {
			var result = authService.refresh(refreshJti);
			addRefreshCookie(response, result.refreshJti(), result.refreshExpiresAt());
			return ResponseEntity.ok(new RefreshResponse(result.accessToken(), result.refreshExpiresAt().getEpochSecond()));
		} catch (AuthService.RefreshReuseDetectedException e) {
			clearRefreshCookie(response);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
	}

	private void addRefreshCookie(HttpServletResponse response, String jti, Instant expiresAt) {
		Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, jti);
		cookie.setHttpOnly(true);
		cookie.setPath("/api/v2/auth");
		cookie.setMaxAge(COOKIE_MAX_AGE_14_DAYS);
		cookie.setAttribute("SameSite", "Lax");
		response.addCookie(cookie);
	}

	private void clearRefreshCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, "");
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		cookie.setPath("/api/v2/auth");
		cookie.setMaxAge(0);
		response.addCookie(cookie);
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

	private void validateOrigin(HttpServletRequest request) {
		String origin = request.getHeader("Origin");
		String referer = request.getHeader("Referer");
		boolean allowed = false;
		for (String allowedOrigin : authProperties.getAllowedOriginsArray()) {
			if (allowedOrigin.equals(origin) || (referer != null && referer.startsWith(allowedOrigin))) {
				allowed = true;
				break;
			}
		}
		if (!allowed) {
			throw new InvalidOriginException();
		}
	}

	public record RefreshResponse(String accessToken, long expiresIn) {}

	public static class InvalidOriginException extends RuntimeException {}
}
