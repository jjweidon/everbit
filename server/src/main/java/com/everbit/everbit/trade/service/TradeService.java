package com.everbit.everbit.trade.service;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.everbit.everbit.trade.dto.TradeResponse;
import com.everbit.everbit.trade.entity.Trade;
import com.everbit.everbit.trade.entity.enums.TradeStatus;
import com.everbit.everbit.trade.entity.enums.TradeType;
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
        Trade trade = Trade.builder()
            .user(user)
            .market(market)
            .type(TradeType.valueOf(orderResponse.side().toUpperCase()))
            .orderId(orderResponse.uuid())
            .price(price)
            .amount(new BigDecimal(orderResponse.volume()))
            .totalPrice(price.multiply(new BigDecimal(orderResponse.volume())))
            .status(TradeStatus.PENDING)
            .signalType(signalType)
            .build();

        return tradeRepository.save(trade);
    }
}
