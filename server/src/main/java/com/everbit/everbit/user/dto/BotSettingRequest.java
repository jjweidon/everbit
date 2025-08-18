package com.everbit.everbit.user.dto;

import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.trade.entity.enums.Strategy;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;
import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record BotSettingRequest(
    Strategy buyStrategy,
    Strategy sellStrategy,
    List<Market> marketList,
    Long buyBaseOrderAmount,
    Long buyMaxOrderAmount,
    Long sellBaseOrderAmount,
    Long sellMaxOrderAmount,
    BigDecimal lossThreshold,
    BigDecimal profitThreshold,
    BigDecimal lossSellRatio,
    BigDecimal profitSellRatio,
    Boolean isLossManagementActive,
    Boolean isProfitTakingActive,
    Boolean isTimeOutSellActive,
    int timeOutSellMinutes,
    BigDecimal timeOutSellProfitRatio
) {}
