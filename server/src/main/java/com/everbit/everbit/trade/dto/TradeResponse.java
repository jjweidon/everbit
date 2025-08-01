package com.everbit.everbit.trade.dto;

import com.everbit.everbit.global.dto.Response;
import com.everbit.everbit.trade.entity.Trade;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TradeResponse(
    String tradeId,
    String orderId,
    String market,
    String strategy,
    String type,
    BigDecimal price,
    BigDecimal amount,
    BigDecimal totalPrice,
    String status,
    LocalDateTime updatedAt
) implements Response {
    public static TradeResponse from(Trade trade) {
        return TradeResponse.builder()
            .tradeId(trade.getId())
            .orderId(trade.getOrderId())
            .market(trade.getMarket().name())
            .strategy(trade.getStrategy().name())
            .type(trade.getType().getDescription())
            .price(trade.getPrice())
            .amount(trade.getAmount())
            .totalPrice(trade.getTotalPrice())
            .status(trade.getStatus().getDescription())
            .updatedAt(trade.getUpdatedAt())
            .build();
    }

    public static List<TradeResponse> from(List<Trade> trades) {
        return trades.stream()
            .map(TradeResponse::from)
            .toList();
    }
}
