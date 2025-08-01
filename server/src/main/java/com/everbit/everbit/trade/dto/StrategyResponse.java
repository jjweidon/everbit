package com.everbit.everbit.trade.dto;

import com.everbit.everbit.global.dto.Response;
import com.everbit.everbit.trade.entity.enums.Strategy;

import lombok.Builder;
import java.util.List;
import java.util.Arrays;

@Builder
public record StrategyResponse(
    String name,
    String value,
    String description
) implements Response {
    public static List<StrategyResponse> getAll() {
        return Arrays.stream(Strategy.values())
            .map(strategy -> StrategyResponse.builder()
                .name(strategy.name())
                .value(strategy.getValue())
                .description(strategy.getDescription())
                .build())
            .toList();
    }

    public static List<StrategyResponse> getUserConfigurableStrategies() {
        return Arrays.stream(Strategy.values())
            .filter(Strategy::isUserConfigurable)
            .map(strategy -> StrategyResponse.builder()
                .name(strategy.name())
                .value(strategy.getValue())
                .description(strategy.getDescription())
                .build())
            .toList();
    }
}
