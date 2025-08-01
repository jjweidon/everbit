package com.everbit.everbit.trade.dto;

import com.everbit.everbit.trade.entity.enums.Strategy;

import lombok.Builder;
import java.util.List;
import java.util.Arrays;

@Builder
public record StrategyResponse(
    String name,
    String value,
    String description
) {
    public static List<StrategyResponse> getAll() {
        return Arrays.stream(Strategy.values())
            .map(strategy -> StrategyResponse.builder()
                .name(strategy.name())
                .value(strategy.getValue())
                .description(strategy.getDescription())
                .build())
            .toList();
    }
}
