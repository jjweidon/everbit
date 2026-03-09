package com.everbit.everbit.auth.api;

import com.everbit.everbit.auth.application.AuthService;
import com.everbit.everbit.auth.application.TokenProvider;
import com.everbit.everbit.auth.config.AuthTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Auth API 스펙 테스트. addFilters=false로 보안 필터 제외, JSON/redirect/status만 검증.
 * SoT: docs/integrations/kakao-oauth-auth-flow.md, docs/testing/backend-tdd-template.md §5.
 */
@WebMvcTest(AuthController.class)
@Import(AuthTestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	AuthService authService;

	@MockBean
	TokenProvider tokenProvider;

	@Test
	void start_redirectsToKakao() throws Exception {
		when(authService.buildOAuthStartRedirectUrl())
			.thenReturn("https://kauth.kakao.com/oauth/authorize?client_id=test&redirect_uri=test&response_type=code&state=abc&scope=profile_nickname");

		mockMvc.perform(get("/api/v2/auth/start"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrlPattern("https://kauth.kakao.com/oauth/authorize*"));
	}

	@Test
	void refresh_withoutCookie_returns401() throws Exception {
		mockMvc.perform(post("/api/v2/auth/refresh")
				.header("Origin", "http://localhost:3000"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void logout_withoutCookie_returns204() throws Exception {
		mockMvc.perform(post("/api/v2/auth/logout")
				.header("Origin", "http://localhost:3000"))
			.andExpect(status().isNoContent());
	}
}
