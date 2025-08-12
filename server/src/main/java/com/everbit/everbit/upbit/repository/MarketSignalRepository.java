package com.everbit.everbit.upbit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.upbit.entity.MarketSignal;
import java.util.Optional;

public interface MarketSignalRepository extends JpaRepository<MarketSignal, String> {
    Optional<MarketSignal> findByMarket(Market market);
}
