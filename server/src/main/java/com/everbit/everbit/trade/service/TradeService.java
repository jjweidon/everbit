package com.everbit.everbit.trade.service;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.everbit.everbit.trade.dto.TradeResponse;
import com.everbit.everbit.trade.entity.Trade;
import com.everbit.everbit.trade.entity.enums.TradeStatus;
import com.everbit.everbit.trade.repository.TradeRepository;
import com.everbit.everbit.upbit.dto.exchange.OrderResponse;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.trade.entity.enums.Strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeService {
    private final TradeRepository tradeRepository;

    public List<Trade> getTrades(User user) {
        return tradeRepository.findByUser(user);
    }

    @Transactional
    public Trade saveTrade(User user, String market, Strategy strategy, OrderResponse orderResponse, BigDecimal price) {
        Trade trade = Trade.of(user, market, strategy, orderResponse, price);
        return tradeRepository.save(trade);
    }

    public List<Trade> findWaitingTrades() {
        return tradeRepository.findByStatus(TradeStatus.WAIT);
    }

    @Transactional
    public void updateTradeStatus(Trade trade, TradeStatus newStatus) {
        trade.updateStatus(newStatus);
        tradeRepository.save(trade);
        log.info("Trade status updated - ID: {}, New Status: {}", trade.getId(), newStatus);
    }
}
