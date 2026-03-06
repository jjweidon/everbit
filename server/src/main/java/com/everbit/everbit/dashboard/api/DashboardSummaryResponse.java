package com.everbit.everbit.dashboard.api;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * GET /api/v2/dashboard/summary 응답.
 * SoT: docs/api/contracts.md §2, client/src/types/api-contracts.ts DashboardSummary.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record DashboardSummaryResponse(
	boolean accountEnabled,
	String strategyKey,
	boolean strategyEnabled,
	String wsStatus,
	String lastReconcileAt,
	String lastErrorAt,
	RiskDto risk,
	EquityDto equity
) {
	public record RiskDto(
		int throttled429Count24h,
		String blocked418Until,
		int unknownAttempts24h,
		java.util.List<String> suspendedMarkets
	) {}

	public record EquityDto(
		String equityKrw,
		String realizedPnlKrw,
		String unrealizedPnlKrw
	) {}
}
