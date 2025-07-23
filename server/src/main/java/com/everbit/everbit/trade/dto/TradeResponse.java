package com.everbit.everbit.trade.dto;

import com.everbit.everbit.trade.entity.Trade;
import com.everbit.everbit.trade.entity.enums.TradeType;
import com.everbit.everbit.trade.entity.enums.TradeStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TradeResponse(
    String tradeId,
    String market,
    TradeType type,
    String orderId,
    BigDecimal price,
    BigDecimal amount,
    BigDecimal totalPrice,
    TradeStatus status,
    String signalType
) {
    public static TradeResponse from(Trade trade) {
        return TradeResponse.builder()
            .tradeId(trade.getId())
            .market(trade.getMarket())
            .type(trade.getType())
            .orderId(trade.getOrderId())
            .price(trade.getPrice())
            .amount(trade.getAmount())
            .totalPrice(trade.getTotalPrice())
            .status(trade.getStatus())
            .signalType(trade.getSignalType().getDescription())
            .build();
    }

    public static List<TradeResponse> from(List<Trade> trades) {
        return trades.stream()
            .map(TradeResponse::from)
            .collect(Collectors.toList());
    }
}
