package com.everbit.everbit.auth.config;

import com.everbit.everbit.auth.application.AuthService;
import com.everbit.everbit.user.application.NotOwnerException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * OAuth2 로그인 성공 시: 회원 매핑, Access/Refresh 발급, 프론트 리다이렉트.
 * Spring Security 기본 콜백(/login/oauth2/code/kakao) 처리 후 호출됨.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private static final String REFRESH_COOKIE_NAME = "everbit_refresh";
	private static final int COOKIE_MAX_AGE_14_DAYS = 14 * 24 * 60 * 60;

	private final AuthService authService;
	private final AuthProperties authProperties;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException {
		OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
		String kakaoId = extractKakaoId(oauth2User);
		String email = extractEmail(oauth2User);

		try {
			var result = authService.handleOAuth2Success(kakaoId, email);
			addRefreshCookie(response, result.refreshJti(), result.refreshExpiresAt());
			String redirectUrl = authProperties.frontendBaseUrl() + "/auth/complete#access_token="
				+ URLEncoder.encode(result.accessToken(), StandardCharsets.UTF_8);
			getRedirectStrategy().sendRedirect(request, response, redirectUrl);
		} catch (NotOwnerException e) {
			redirectToLoginWithError(response, e.getMessage());
		} catch (Exception e) {
			log.warn("OAuth2 success handler error", e);
			redirectToLoginWithError(response, "일시적인 오류가 발생했습니다.");
		}
	}

	private static String extractKakaoId(OAuth2User oauth2User) {
		Object idAttr = oauth2User.getAttributes().get("id");
		if (idAttr == null) return "";
		if (idAttr instanceof Number n) return String.valueOf(n.longValue());
		return idAttr.toString();
	}

	private static String extractEmail(OAuth2User oauth2User) {
		Object kakaoAccount = oauth2User.getAttribute("kakao_account");
		if (kakaoAccount instanceof Map<?, ?> map && map.get("email") != null) {
			return map.get("email").toString();
		}
		return null;
	}

	private void addRefreshCookie(HttpServletResponse response, String jti, java.time.Instant expiresAt) {
		Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, jti);
		cookie.setHttpOnly(true);
		cookie.setPath("/api/v2/auth");
		cookie.setMaxAge(COOKIE_MAX_AGE_14_DAYS);
		cookie.setAttribute("SameSite", "Lax");
		response.addCookie(cookie);
	}

	private void redirectToLoginWithError(HttpServletResponse response, String message) throws IOException {
		String url = authProperties.frontendBaseUrl() + "/login?error="
			+ URLEncoder.encode(message, StandardCharsets.UTF_8);
		response.sendRedirect(url);
	}
}
