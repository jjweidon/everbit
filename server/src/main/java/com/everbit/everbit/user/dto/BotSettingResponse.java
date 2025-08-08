package com.everbit.everbit.user.dto;

import com.everbit.everbit.user.entity.BotSetting;
import com.everbit.everbit.global.dto.Response;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.everbit.everbit.trade.entity.enums.Market;

import java.util.List;

import lombok.Builder;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record BotSettingResponse(
    String botSettingId,
    String buyStrategy,
    String sellStrategy,
    List<Market> marketList,
    Long buyBaseOrderAmount,
    Long buyMaxOrderAmount,
    Long sellBaseOrderAmount,
    Long sellMaxOrderAmount
) implements Response {
    public static BotSettingResponse from(BotSetting botSetting) {
        return BotSettingResponse.builder()
            .botSettingId(botSetting.getId())
            .buyStrategy(botSetting.getBuyStrategy().name())
            .sellStrategy(botSetting.getSellStrategy().name())
            .marketList(botSetting.getMarketList())
            .buyBaseOrderAmount(botSetting.getBuyBaseOrderAmount())
            .buyMaxOrderAmount(botSetting.getBuyMaxOrderAmount())
            .sellBaseOrderAmount(botSetting.getSellBaseOrderAmount())
            .sellMaxOrderAmount(botSetting.getSellMaxOrderAmount())
            .build();
    }
}
