package com.everbit.everbit.user.dto;

import com.everbit.everbit.user.entity.BotSetting;
import com.everbit.everbit.global.dto.Response;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.everbit.everbit.trade.entity.enums.Market;

import java.util.List;
import java.math.BigDecimal;

import lombok.Builder;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record BotSettingResponse(
    String botSettingId,
    List<Market> marketList,
    Boolean isBuyActive,
    Boolean isSellActive,
    String buyStrategy,
    String sellStrategy,
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
) implements Response {
    public static BotSettingResponse from(BotSetting botSetting) {
        return BotSettingResponse.builder()
            .botSettingId(botSetting.getId())
            .marketList(botSetting.getMarketList())
            .isBuyActive(botSetting.getIsBuyActive())
            .isSellActive(botSetting.getIsSellActive())
            .buyStrategy(botSetting.getBuyStrategy().name())
            .sellStrategy(botSetting.getSellStrategy().name())
            .buyBaseOrderAmount(botSetting.getBuyBaseOrderAmount())
            .buyMaxOrderAmount(botSetting.getBuyMaxOrderAmount())
            .sellBaseOrderAmount(botSetting.getSellBaseOrderAmount())
            .sellMaxOrderAmount(botSetting.getSellMaxOrderAmount())
            .lossThreshold(botSetting.getLossThreshold())
            .profitThreshold(botSetting.getProfitThreshold())
            .lossSellRatio(botSetting.getLossSellRatio())
            .profitSellRatio(botSetting.getProfitSellRatio())
            .isLossManagementActive(botSetting.getIsLossManagementActive())
            .isProfitTakingActive(botSetting.getIsProfitTakingActive())
            .isTimeOutSellActive(botSetting.getIsTimeOutSellActive())
            .timeOutSellMinutes(botSetting.getTimeOutSellMinutes())
            .timeOutSellProfitRatio(botSetting.getTimeOutSellProfitRatio())
            .build();
    }
}
