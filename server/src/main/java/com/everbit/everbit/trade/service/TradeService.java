package com.everbit.everbit.trade.service;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.everbit.everbit.trade.dto.TradeResponse;
import com.everbit.everbit.trade.entity.Trade;
import com.everbit.everbit.trade.repository.TradeRepository;
import com.everbit.everbit.upbit.dto.exchange.OrderResponse;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.trade.entity.enums.SignalType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeService {
    private final TradeRepository tradeRepository;

    public List<TradeResponse> getTrades() {
        return TradeResponse.from(tradeRepository.findAll());
    }

    @Transactional
    public Trade saveTrade(User user, String market, OrderResponse orderResponse, BigDecimal price, SignalType signalType) {
        Trade trade = Trade.of(user, market, orderResponse, price, signalType);
        return tradeRepository.save(trade);
    }
}
