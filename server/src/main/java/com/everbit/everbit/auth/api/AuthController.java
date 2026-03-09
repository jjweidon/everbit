package com.everbit.everbit.auth.api;

import com.everbit.everbit.auth.application.AuthService;
import com.everbit.everbit.auth.config.AuthProperties;
import com.everbit.everbit.user.application.NotOwnerException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * 인증 API. SoT: docs/api/contracts.md, docs/integrations/kakao-oauth-auth-flow.md.
 */
@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
public class AuthController {

	private static final String REFRESH_COOKIE_NAME = "everbit_refresh";
	private static final int COOKIE_MAX_AGE_14_DAYS = 14 * 24 * 60 * 60;

	private final AuthService authService;
	private final AuthProperties authProperties;

	@GetMapping("/start")
	public void start(HttpServletResponse response) throws IOException {
		String redirectUrl = authService.buildOAuthStartRedirectUrl();
		response.sendRedirect(redirectUrl);
	}

	@GetMapping("/callback")
	public void callback(
		@RequestParam(required = false) String code,
		@RequestParam(required = false) String state,
		@RequestParam(required = false) String error,
		HttpServletResponse response
	) throws IOException {
		if (error != null) {
			redirectToLoginWithError(response, "카카오 로그인이 취소되었습니다.");
			return;
		}
		if (code == null || state == null) {
			redirectToLoginWithError(response, "인증 코드가 없습니다.");
			return;
		}

		try {
			var result = authService.handleCallback(code, state);
			addRefreshCookie(response, result.refreshJti(), result.refreshExpiresAt());
			String redirectUrl = authProperties.frontendBaseUrl() + "/auth/complete#access_token="
				+ URLEncoder.encode(result.accessToken(), StandardCharsets.UTF_8);
			response.sendRedirect(redirectUrl);
		} catch (AuthService.InvalidOAuthStateException e) {
			redirectToLoginWithError(response, e.getMessage());
		} catch (NotOwnerException e) {
			redirectToLoginWithError(response, e.getMessage());
		} catch (Exception e) {
			redirectToLoginWithError(response, "일시적인 오류가 발생했습니다.");
		}
	}

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

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
		validateOrigin(request);

		String refreshJti = getRefreshCookie(request);
		if (refreshJti != null) {
			authService.logout(refreshJti);
		}
		clearRefreshCookie(response);
		return ResponseEntity.noContent().build();
	}

	private void addRefreshCookie(HttpServletResponse response, String jti, Instant expiresAt) {
		Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, jti);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
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

	private void redirectToLoginWithError(HttpServletResponse response, String message) throws IOException {
		String url = authProperties.frontendBaseUrl() + "/login?error=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
		response.sendRedirect(url);
	}

	public record RefreshResponse(String accessToken, long expiresIn) {}

	public static class InvalidOriginException extends RuntimeException {}
}
