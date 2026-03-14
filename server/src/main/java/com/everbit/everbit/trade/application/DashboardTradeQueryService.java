package com.everbit.everbit.trade.application;

import com.everbit.everbit.trade.domain.OrderAttemptStatus;
import com.everbit.everbit.trade.repository.OrderAttemptRepository;
import com.everbit.everbit.trade.repository.PnlSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * 대시보드용 trade 집계 조회. risk(429/UNKNOWN 건수), equity(PnL 스냅샷).
 * SoT: docs/architecture/modular-monolith.md §3.3 (공개 API).
 */
@Service
@RequiredArgsConstructor
public class DashboardTradeQueryService {

	private final OrderAttemptRepository orderAttemptRepository;
	private final PnlSnapshotRepository pnlSnapshotRepository;

	@Transactional(readOnly = true)
	public DashboardRiskSummary getRiskSummary(Long ownerId, List<String> suspendedMarkets) {
		Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
		int throttled = (int) orderAttemptRepository.countByOwner_IdAndStatusAndCreatedAtAfter(
			ownerId, OrderAttemptStatus.THROTTLED, since);
		int unknown = (int) orderAttemptRepository.countByOwner_IdAndStatusAndCreatedAtAfter(
			ownerId, OrderAttemptStatus.UNKNOWN, since);
		return DashboardRiskSummary.builder()
			.throttled429Count24h(throttled)
			.blocked418Until(null)
			.unknownAttempts24h(unknown)
			.suspendedMarkets(suspendedMarkets)
			.build();
	}

	@Transactional(readOnly = true)
	public Optional<DashboardEquitySummary> getEquitySummary(Long ownerId) {
		Optional<Instant> latest = pnlSnapshotRepository.findTop1ByOwner_IdOrderByCapturedAtDesc(ownerId)
			.map(p -> p.getCapturedAt());
		if (latest.isEmpty()) {
			return Optional.empty();
		}
		BigDecimal equity = BigDecimal.ZERO;
		BigDecimal realized = BigDecimal.ZERO;
		BigDecimal unrealized = BigDecimal.ZERO;
		for (var p : pnlSnapshotRepository.findByOwner_IdAndCapturedAt(ownerId, latest.get())) {
			equity = equity.add(p.getEquity());
			realized = realized.add(p.getRealizedPnl());
			unrealized = unrealized.add(p.getUnrealizedPnl());
		}
		return Optional.of(DashboardEquitySummary.builder()
			.equityKrw(equity)
			.realizedPnlKrw(realized)
			.unrealizedPnlKrw(unrealized)
			.build());
	}
}
