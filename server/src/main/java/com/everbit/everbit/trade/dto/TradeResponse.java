package com.everbit.everbit.trade.dto;

import com.everbit.everbit.trade.entity.Trade;

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
    String type,
    String orderId,
    BigDecimal price,
    BigDecimal amount,
    BigDecimal totalPrice,
    String status,
    String signalType,
    String updatedAt
) {
    public static TradeResponse from(Trade trade) {
        return TradeResponse.builder()
            .tradeId(trade.getId())
            .market(trade.getMarket().getCode())
            .type(trade.getType().getDescription())
            .orderId(trade.getOrderId())
            .price(trade.getPrice())
            .amount(trade.getAmount())
            .totalPrice(trade.getTotalPrice())
            .status(trade.getStatus().getDescription())
            .signalType(trade.getSignalType().getDescription())
            .updatedAt(trade.getUpdatedAt().toString())
            .build();
    }

    public static List<TradeResponse> from(List<Trade> trades) {
        return trades.stream()
            .map(TradeResponse::from)
            .collect(Collectors.toList());
    }
}
