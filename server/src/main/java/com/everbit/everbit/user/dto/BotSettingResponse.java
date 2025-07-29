package com.everbit.everbit.user.dto;

import com.everbit.everbit.user.entity.BotSetting;
import com.everbit.everbit.global.dto.Response;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.trade.entity.enums.CandleInterval;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record BotSettingResponse(
    String botSettingId,
    String strategy,
    List<Market> marketList,
    Long baseOrderAmount,
    Long maxOrderAmount,
    LocalDateTime startTime,
    LocalDateTime endTime,
    CandleInterval candleInterval,
    Integer candleCount
) implements Response {
    public static BotSettingResponse from(BotSetting botSetting) {
        return BotSettingResponse.builder()
            .botSettingId(botSetting.getId())
            .strategy(botSetting.getStrategy().name())
            .marketList(botSetting.getMarketList())
            .baseOrderAmount(botSetting.getBaseOrderAmount())
            .maxOrderAmount(botSetting.getMaxOrderAmount())
            .startTime(botSetting.getStartTime())
            .endTime(botSetting.getEndTime())
            .candleInterval(botSetting.getCandleInterval())
            .candleCount(botSetting.getCandleCount())
            .build();
    }
}
