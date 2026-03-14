package com.everbit.everbit.strategy.repository;

import com.everbit.everbit.strategy.domain.MarketState;
import com.everbit.everbit.strategy.domain.MarketStateId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 마켓 실행 상태 저장소. SoT: docs/architecture/data-model.md §2.7.
 */
public interface MarketStateRepository extends JpaRepository<MarketState, MarketStateId> {

	List<MarketState> findByIdOwnerIdAndTradeStatus(Long ownerId, String tradeStatus);
}
