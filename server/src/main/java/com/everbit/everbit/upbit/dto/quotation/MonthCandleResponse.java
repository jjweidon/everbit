package com.everbit.everbit.upbit.dto.quotation;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.OffsetDateTime;
import java.time.LocalDate;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MonthCandleResponse(
    String market,
    OffsetDateTime candleDateTimeUtc,
    OffsetDateTime candleDateTimeKst,
    Double openingPrice,
    Double highPrice,
    Double lowPrice,
    Double tradePrice,
    Long timestamp,
    Double candleAccTradePrice,
    Double candleAccTradeVolume,
    LocalDate firstDayOfPeriod
) {} 