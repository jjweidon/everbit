package com.everbit.everbit.dashboard.api;

import com.everbit.everbit.auth.config.JwtAuthenticationFilter;
import com.everbit.everbit.auth.infrastructure.JwtTokenProvider;
import com.everbit.everbit.auth.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 대시보드 엔드포인트 인가 규칙 검증.
 * SoT: docs/testing/tdd.md §4.8, docs/testing/backend-tdd-template.md §5.
 * 프레임워크를 테스트하지 않고, 우리 프로젝트의 인가 규칙만 검증.
 */
@WebMvcTest(DashboardController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@ActiveProfiles("test")
class DashboardControllerSecurityTest {

	@Autowired
	MockMvc mockMvc;

	@org.springframework.boot.test.mock.mockito.MockBean
	JwtTokenProvider tokenProvider;

	@Test
	void 인증없으면_401() throws Exception {
		mockMvc.perform(get("/api/v2/dashboard/summary").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "USER")
	void 인증있으면_200() throws Exception {
		mockMvc.perform(get("/api/v2/dashboard/summary").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
	}

	@Test
	void Bearer_토큰_유효하면_200() throws Exception {
		when(tokenProvider.parseOwnerId(anyString())).thenReturn(1L);

		mockMvc.perform(get("/api/v2/dashboard/summary")
				.accept(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer valid-token"))
			.andExpect(status().isOk());
	}
}
