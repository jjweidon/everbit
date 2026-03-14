package com.everbit.everbit.dashboard.application;

import com.everbit.everbit.dashboard.api.DashboardSummaryResponse;
import com.everbit.everbit.strategy.application.MarketStateQueryService;
import com.everbit.everbit.strategy.application.StrategyConfigQueryService;
import com.everbit.everbit.trade.application.DashboardEquitySummary;
import com.everbit.everbit.trade.application.DashboardRiskSummary;
import com.everbit.everbit.trade.application.DashboardTradeQueryService;
import com.everbit.everbit.user.application.KillSwitchQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 대시보드 요약 조회. DB/엔티티와 연동.
 * SoT: docs/api/contracts.md §2, docs/architecture/modular-monolith.md §3.3,
 * docs/strategies/everbit_master_spec.md (EXTREME_FLIP, STRUCTURE_LIFT, PRESSURE_SURGE 등).
 * user, strategy, trade 모듈의 공개 조회 API만 호출.
 */
@Service
@RequiredArgsConstructor
public class DashboardSummaryService {

	private static final String WS_STATUS_CONNECTED = "CONNECTED";

	private final KillSwitchQueryService killSwitchQueryService;
	private final StrategyConfigQueryService strategyConfigQueryService;
	private final MarketStateQueryService marketStateQueryService;
	private final DashboardTradeQueryService dashboardTradeQueryService;

	/**
	 * ownerId 기준으로 대시보드 요약 조회. 계약 §2 필드 전부 실데이터 반환.
	 * strategyKey는 strategy_config에서 조회(없으면 EXTREME_FLIP), strategyEnabled는 KillSwitch 허용 목록 기준.
	 */
	@Transactional(readOnly = true)
	public DashboardSummaryResponse getSummary(Long ownerId) {
		boolean accountEnabled = killSwitchQueryService.isAccountEnabled(ownerId);
		String strategyKey = strategyConfigQueryService.getPrimaryStrategyKeyForDashboard(ownerId);
		boolean strategyEnabled = killSwitchQueryService.isStrategyEnabled(ownerId, strategyKey);
		List<String> suspendedMarkets = marketStateQueryService.findSuspendedMarkets(ownerId);
		DashboardRiskSummary risk = dashboardTradeQueryService.getRiskSummary(ownerId, suspendedMarkets);
		DashboardEquitySummary equity = dashboardTradeQueryService.getEquitySummary(ownerId)
			.orElse(DashboardEquitySummary.builder()
				.equityKrw(java.math.BigDecimal.ZERO)
				.realizedPnlKrw(java.math.BigDecimal.ZERO)
				.unrealizedPnlKrw(java.math.BigDecimal.ZERO)
				.build());

		return new DashboardSummaryResponse(
			accountEnabled,
			strategyKey,
			strategyEnabled,
			WS_STATUS_CONNECTED,
			null,
			null,
			new DashboardSummaryResponse.RiskDto(
				risk.getThrottled429Count24h(),
				risk.getBlocked418Until(),
				risk.getUnknownAttempts24h(),
				risk.getSuspendedMarkets()
			),
			new DashboardSummaryResponse.EquityDto(
				equity.getEquityKrw().toPlainString(),
				equity.getRealizedPnlKrw().toPlainString(),
				equity.getUnrealizedPnlKrw().toPlainString()
			)
		);
	}
}
