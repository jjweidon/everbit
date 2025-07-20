package com.everbit.everbit.upbit.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OrderChanceResponse(
    String bidFee,
    String askFee,
    String makerBidFee,
    String makerAskFee,
    Market market,
    Account bidAccount,
    Account askAccount
) {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Market(
        String id,
        String name,
        List<String> orderTypes,
        List<String> orderSides,
        List<String> bidTypes,
        List<String> askTypes,
        Bid bid,
        Ask ask,
        String maxTotal,
        String state
    ) {
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public record Bid(
            String currency,
            String minTotal
        ) {}

        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public record Ask(
            String currency,
            String minTotal
        ) {}
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Account(
        String currency,
        String balance,
        String locked,
        String avgBuyPrice,
        boolean avgBuyPriceModified,
        String unitCurrency
    ) {}
} 