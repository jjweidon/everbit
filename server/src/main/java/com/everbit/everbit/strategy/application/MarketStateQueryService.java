package com.everbit.everbit.strategy.application;

import com.everbit.everbit.strategy.repository.MarketStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 마켓 상태 조회. 대시보드 등에서 SUSPENDED 마켓 목록 등 조회용.
 * SoT: docs/architecture/modular-monolith.md §3.3 (공개 API).
 */
@Service
@RequiredArgsConstructor
public class MarketStateQueryService {

	private static final String SUSPENDED = "SUSPENDED";

	private final MarketStateRepository marketStateRepository;

	@Transactional(readOnly = true)
	public List<String> findSuspendedMarkets(Long ownerId) {
		return marketStateRepository.findByIdOwnerIdAndTradeStatus(java.util.Objects.requireNonNull(ownerId), SUSPENDED)
			.stream()
			.map(ms -> ms.getId().getMarket())
			.collect(Collectors.toList());
	}
}
