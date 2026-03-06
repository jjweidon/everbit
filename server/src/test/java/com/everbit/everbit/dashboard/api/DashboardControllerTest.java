package com.everbit.everbit.dashboard.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * GET /api/v2/dashboard/summary stub 응답 검증.
 * SoT: docs/api/contracts.md §2, docs/testing/backend-tdd-template.md.
 */
@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Test
	void summary_returnsStubWithContractFields() throws Exception {
		mockMvc.perform(get("/api/v2/dashboard/summary").accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
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
