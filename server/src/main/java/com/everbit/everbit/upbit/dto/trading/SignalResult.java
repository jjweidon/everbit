package com.everbit.everbit.upbit.dto.trading;

import lombok.Builder;

@Builder
public record SignalResult(
    boolean signalGenerated,
    double signalStrength
) {
    public static SignalResult of(boolean signalGenerated, double signalStrength) {
        return SignalResult.builder()
            .signalGenerated(signalGenerated)
            .signalStrength(signalStrength)
            .build();
    }
}
