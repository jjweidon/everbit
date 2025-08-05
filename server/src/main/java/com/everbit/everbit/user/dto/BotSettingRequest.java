package com.everbit.everbit.user.dto;

import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.trade.entity.enums.Strategy;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record BotSettingRequest(
    Strategy buyStrategy,
    Strategy sellStrategy,
    List<Market> marketList,
    Long baseOrderAmount,
    Long maxOrderAmount
) {}
