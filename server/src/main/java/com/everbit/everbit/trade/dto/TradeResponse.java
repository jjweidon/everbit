package com.everbit.everbit.trade.dto;

import com.everbit.everbit.trade.entity.Trade;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TradeResponse(
    String tradeId,
    String orderId,
    String market,
    String type,
    BigDecimal price,
    BigDecimal amount,
    BigDecimal totalPrice,
    String status,
    String strategy,
    String updatedAt
) {
    public static TradeResponse from(Trade trade) {
        return TradeResponse.builder()
            .tradeId(trade.getId())
            .orderId(trade.getOrderId())
            .market(trade.getMarket().getCode())
            .type(trade.getType().getValue())
            .price(trade.getPrice())
            .amount(trade.getAmount())
            .totalPrice(trade.getPrice().multiply(trade.getAmount()))
            .status(trade.getStatus().getValue())
            .strategy(trade.getStrategy().name())
            .updatedAt(trade.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
            .build();
    }

    public static List<TradeResponse> from(List<Trade> trades) {
        return trades.stream()
            .map(TradeResponse::from)
            .collect(Collectors.toList());
    }
}
