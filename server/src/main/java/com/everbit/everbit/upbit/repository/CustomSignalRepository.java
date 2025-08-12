package com.everbit.everbit.upbit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.upbit.entity.CustomSignal;
import java.util.Optional;

public interface CustomSignalRepository extends JpaRepository<CustomSignal, String> {
    Optional<CustomSignal> findByMarket(Market market);
}
