package com.everbit.everbit.upbit.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import com.everbit.everbit.upbit.repository.MarketSignalRepository;
import com.everbit.everbit.upbit.entity.MarketSignal;
import com.everbit.everbit.trade.entity.enums.Market;

@Service
@RequiredArgsConstructor
public class MarketSignalService {
    private final MarketSignalRepository marketSignalRepository;

    public MarketSignal findOrCreateMarketSignal(Market market) {
        return marketSignalRepository.findByMarket(market)
            .orElseGet(() -> {
                MarketSignal marketSignal = MarketSignal.builder()
                    .market(market)
                    .build();
                return marketSignalRepository.save(marketSignal);
            });
    }
}
