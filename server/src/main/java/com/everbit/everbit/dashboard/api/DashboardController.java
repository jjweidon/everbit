package com.everbit.everbit.dashboard.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.everbit.everbit.auth.support.CurrentOwnerId;
import com.everbit.everbit.dashboard.application.DashboardSummaryService;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

/**
 * 대시보드 API. SoT: docs/api/contracts.md §2, docs/architecture/modular-monolith.md.
 * 요청: Authorization: Bearer &lt;token&gt; 필수 (SecurityConfig).
 * ownerId는 JWT에서 취득하여 {@link CurrentOwnerId}로 주입.
 */
@RestController
@RequestMapping("/api/v2/dashboard")
@RequiredArgsConstructor
public class DashboardController {

	private final DashboardSummaryService dashboardSummaryService;

	@GetMapping("/summary")
	public ResponseEntity<DashboardSummaryResponse> summary(@CurrentOwnerId @NonNull Long ownerId) {
		return ResponseEntity.ok(dashboardSummaryService.getSummary(ownerId));
	}
}
