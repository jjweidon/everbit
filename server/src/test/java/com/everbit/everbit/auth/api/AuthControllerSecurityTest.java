package com.everbit.everbit.auth.api;

import com.everbit.everbit.auth.application.AuthService;
import com.everbit.everbit.auth.infrastructure.JwtTokenProvider;
import com.everbit.everbit.auth.config.AuthTestConfig;
import com.everbit.everbit.auth.config.JwtAuthenticationFilter;
import com.everbit.everbit.auth.config.LogoutRefreshInvalidationHandler;
import com.everbit.everbit.auth.config.LogoutSuccessRedirectHandler;
import com.everbit.everbit.auth.config.OAuth2AuthenticationFailureHandler;
import com.everbit.everbit.auth.config.OAuth2AuthenticationSuccessHandler;
import com.everbit.everbit.auth.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Auth 엔드포인트 인가 규칙 검증. /api/v2/auth/refresh 는 permitAll.
 * 로그인은 /oauth2/authorization/kakao, 로그아웃은 /logout (Spring Security 기본).
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, AuthTestConfig.class})
@ActiveProfiles("test")
class AuthControllerSecurityTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	AuthService authService;

	@MockBean
	JwtTokenProvider tokenProvider;

	@MockBean
	OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler;

	@MockBean
	OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler;

	@MockBean
	LogoutRefreshInvalidationHandler logoutRefreshInvalidationHandler;

	@MockBean
	LogoutSuccessRedirectHandler logoutSuccessRedirectHandler;

	@Test
	void refresh_permitAll_reachesController() throws Exception {
		// permitAll이므로 Security가 막지 않음. 쿠키 없으면 컨트롤러가 401 반환
		mockMvc.perform(post("/api/v2/auth/refresh").header("Origin", "http://localhost:3000"))
			.andExpect(status().isUnauthorized());
	}
}
