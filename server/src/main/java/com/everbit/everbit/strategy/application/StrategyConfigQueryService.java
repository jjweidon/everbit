package com.everbit.everbit.strategy.application;

import com.everbit.everbit.strategy.domain.StrategyConfig;
import com.everbit.everbit.strategy.repository.StrategyConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 전략 설정 조회. 대시보드 등에서 표시할 전략 키(EXTREME_FLIP, STRUCTURE_LIFT, PRESSURE_SURGE 등) 조회.
 * SoT: docs/architecture/data-model.md §2.5, docs/strategies/everbit_master_spec.md.
 */
@Service
@RequiredArgsConstructor
public class StrategyConfigQueryService {

	/**
	 * 계약 상 "기본/우선 표시" 전략 키. strategy_config가 없을 때 사용.
	 */
	private static final String DEFAULT_STRATEGY_KEY = "EXTREME_FLIP";

	private final StrategyConfigRepository strategyConfigRepository;

	/**
	 * 대시보드 요약에 쓸 "현재 표시할 전략 키".
	 * owner의 strategy_config 중 첫 번째( strategy_key 오름차순)를 사용하고,
	 * 없으면 DEFAULT_STRATEGY_KEY(EXTREME_FLIP) 반환.
	 */
	@Transactional(readOnly = true)
	public String getPrimaryStrategyKeyForDashboard(Long ownerId) {
		List<StrategyConfig> configs = strategyConfigRepository.findByIdOwnerIdOrderByIdStrategyKeyAsc(
			Objects.requireNonNull(ownerId));
		return configs.isEmpty()
			? DEFAULT_STRATEGY_KEY
			: configs.get(0).getId().getStrategyKey();
	}
}
