package com.everbit.everbit.dashboard.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 대시보드 API (stub). SoT: docs/api/contracts.md §2.
 * 인증은 bootstrap 단계에서 비활성; 추후 Bearer 필수 적용.
 */
@RestController
@RequestMapping("/api/v2/dashboard")
public class DashboardController {

	@GetMapping("/summary")
	public ResponseEntity<DashboardSummaryResponse> summary() {
		DashboardSummaryResponse stub = new DashboardSummaryResponse(
			true,
			"EXTREME_FLIP",
			true,
			"CONNECTED",
			"2026-03-06T02:30:00.000Z",
			"2026-03-06T01:15:00.000Z",
			new DashboardSummaryResponse.RiskDto(2, null, 1, java.util.List.of("KRW-BTC")),
			new DashboardSummaryResponse.EquityDto("12500000", "450000", "-32000")
		);
		return ResponseEntity.ok(stub);
	}
}
