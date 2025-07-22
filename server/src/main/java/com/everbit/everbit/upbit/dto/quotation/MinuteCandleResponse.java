package com.everbit.everbit.upbit.dto.quotation;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MinuteCandleResponse(
    String market,
    LocalDateTime candleDateTimeUtc,
    LocalDateTime candleDateTimeKst,
    Double openingPrice,
    Double highPrice,
    Double lowPrice,
    Double tradePrice,
    Long timestamp,
    Double candleAccTradePrice,
    Double candleAccTradeVolume,
    Integer unit
) {} 