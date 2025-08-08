package com.everbit.everbit.trade.dto;

import com.everbit.everbit.trade.entity.enums.Market;

import lombok.Builder;

@Builder
public record MarketResponse(
    String market,
    String description
) {
    public static MarketResponse from(Market market) {
        return MarketResponse.builder()
            .market(market.name())
            .description(market.getDescription())
            .build();
    }
}
