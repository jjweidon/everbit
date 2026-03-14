package com.everbit.everbit.trade.application;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 대시보드 리스크 요약. SoT: docs/api/contracts.md §2 risk 필드.
 */
@Getter
@Builder
public class DashboardRiskSummary {

	private final int throttled429Count24h;
	private final String blocked418Until;
	private final int unknownAttempts24h;
	private final List<String> suspendedMarkets;
}
