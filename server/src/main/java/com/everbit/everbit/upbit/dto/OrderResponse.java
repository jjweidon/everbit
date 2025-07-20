package com.everbit.everbit.upbit.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OrderResponse(
    String uuid,
    String side,
    String ordType,
    String price,
    String state,
    String market,
    LocalDateTime createdAt,
    String volume,
    String remainingVolume,
    String reservedFee,
    String remainingFee,
    String paidFee,
    String locked,
    String executedVolume,
    Integer tradesCount,
    String preventedVolume,
    String preventedLocked,
    List<Trade> trades
) {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Trade(
        String market,
        String uuid,
        String price,
        String volume,
        String funds,
        String side
    ) {}
} 