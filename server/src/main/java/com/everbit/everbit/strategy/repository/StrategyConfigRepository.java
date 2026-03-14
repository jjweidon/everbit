package com.everbit.everbit.strategy.repository;

import com.everbit.everbit.strategy.domain.StrategyConfig;
import com.everbit.everbit.strategy.domain.StrategyConfigId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 전략 설정 저장소. SoT: docs/architecture/data-model.md §2.5.
 * 전략 키: EXTREME_FLIP, STRUCTURE_LIFT, PRESSURE_SURGE 등.
 */
public interface StrategyConfigRepository extends JpaRepository<StrategyConfig, StrategyConfigId> {

	List<StrategyConfig> findByIdOwnerIdOrderByIdStrategyKeyAsc(Long ownerId);
}
