package com.everbit.everbit.dashboard.api;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.everbit.everbit.auth.config.WebMvcAuthConfig;
import com.everbit.everbit.auth.support.CurrentOwnerIdArgumentResolver;
import com.everbit.everbit.dashboard.application.DashboardSummaryService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GET /api/v2/dashboard/summary stub 응답 검증.
 * SoT: docs/api/contracts.md §2, docs/testing/backend-tdd-template.md.
 * Security 미로드(addFilters=false). @CurrentOwnerId 주입을 위해 principal을 수동 설정.
 */
@WebMvcTest(DashboardController.class)
@ImportAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@Import({WebMvcAuthConfig.class, CurrentOwnerIdArgumentResolver.class})
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	DashboardSummaryService dashboardSummaryService;

	@Test
	void summary_returnsStubWithContractFields() throws Exception {
		DashboardSummaryResponse stub = new DashboardSummaryResponse(
			true,
			"EXTREME_FLIP",
			true,
			"CONNECTED",
			"2026-03-06T02:30:00.000Z",
			"2026-03-06T01:15:00.000Z",
			new DashboardSummaryResponse.RiskDto(2, null, 1, List.of("KRW-BTC")),
			new DashboardSummaryResponse.EquityDto("12500000", "450000", "-32000")
		);
		when(dashboardSummaryService.getSummary(anyLong())).thenReturn(stub);

		MediaType json = Objects.requireNonNull(MediaType.APPLICATION_JSON);
		mockMvc.perform(get("/api/v2/dashboard/summary").accept(json)
				.with(request -> {
					SecurityContextHolder.getContext().setAuthentication(
						new UsernamePasswordAuthenticationToken(1L, null, Collections.emptyList()));
					return request;
				}))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(json))
			.andExpect(jsonPath("$.accountEnabled").value(true))
			.andExpect(jsonPath("$.strategyKey").value("EXTREME_FLIP"))
			.andExpect(jsonPath("$.wsStatus").value("CONNECTED"))
			.andExpect(jsonPath("$.risk.throttled429Count24h").value(2))
			.andExpect(jsonPath("$.risk.unknownAttempts24h").value(1))
			.andExpect(jsonPath("$.risk.suspendedMarkets").isArray())
			.andExpect(jsonPath("$.equity.equityKrw").value("12500000"))
			.andExpect(jsonPath("$.equity.realizedPnlKrw").value("450000"))
			.andExpect(jsonPath("$.equity.unrealizedPnlKrw").value("-32000"));
	}
}
