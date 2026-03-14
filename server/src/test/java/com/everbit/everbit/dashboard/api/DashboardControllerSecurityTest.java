package com.everbit.everbit.dashboard.api;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.everbit.everbit.auth.config.JwtAuthenticationFilter;
import com.everbit.everbit.auth.config.SecurityConfig;
import com.everbit.everbit.auth.config.WebMvcAuthConfig;
import com.everbit.everbit.auth.infrastructure.JwtTokenProvider;
import com.everbit.everbit.auth.support.CurrentOwnerIdArgumentResolver;
import com.everbit.everbit.dashboard.application.DashboardSummaryService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 대시보드 엔드포인트 인가 규칙 검증.
 * SoT: docs/testing/tdd.md §4.8, docs/testing/backend-tdd-template.md §5.
 * 프레임워크를 테스트하지 않고, 우리 프로젝트의 인가 규칙만 검증.
 */
@WebMvcTest(controllers = DashboardController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, WebMvcAuthConfig.class, CurrentOwnerIdArgumentResolver.class})
@ActiveProfiles("test")
class DashboardControllerSecurityTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	JwtTokenProvider tokenProvider;

	@MockBean
	DashboardSummaryService dashboardSummaryService;

	@BeforeEach
	void setUp() {
		DashboardSummaryResponse stub = new DashboardSummaryResponse(
			true,
			"EXTREME_FLIP",
			true,
			"CONNECTED",
			null,
			null,
			new DashboardSummaryResponse.RiskDto(0, null, 0, List.of()),
			new DashboardSummaryResponse.EquityDto("0", "0", "0")
		);
		when(dashboardSummaryService.getSummary(org.mockito.ArgumentMatchers.anyLong())).thenReturn(stub);
	}

	@Test
	void 인증없으면_401() throws Exception {
		mockMvc.perform(get("/api/v2/dashboard/summary").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isUnauthorized());
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
