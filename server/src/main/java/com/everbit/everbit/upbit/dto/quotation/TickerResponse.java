package com.everbit.everbit.upbit.dto.quotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TickerResponse(
    String market,
    String tradeDate,
    String tradeTime,
    String tradeDateKst,
    String tradeTimeKst,
    Long tradeTimestamp,
    Double openingPrice,
    Double highPrice,
    Double lowPrice,
    Double tradePrice,
    Double prevClosingPrice,
    String change,
    Double changePrice,
    Double changeRate,
    Double signedChangePrice,
    Double signedChangeRate,
    Double tradeVolume,
    Double accTradePrice,
    @JsonProperty("acc_trade_price_24h")
    Double accTradePrice24h,
    Double accTradeVolume,
    @JsonProperty("acc_trade_volume_24h")
    Double accTradeVolume24h,
    Double highest52WeekPrice,
    String highest52WeekDate,
    Double lowest52WeekPrice,
    String lowest52WeekDate,
    Long timestamp
) {} 