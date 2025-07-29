package com.everbit.everbit.user.dto;

import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.trade.entity.enums.Strategy;
import com.everbit.everbit.trade.entity.enums.CandleInterval;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record BotSettingRequest(
    Strategy strategy,
    List<Market> marketList,
    Long baseOrderAmount,
    Long maxOrderAmount,
    LocalDateTime startTime,
    LocalDateTime endTime,
    CandleInterval candleInterval,
    Integer candleCount
) {}
