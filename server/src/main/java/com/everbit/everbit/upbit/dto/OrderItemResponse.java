package com.everbit.everbit.upbit.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OrderItemResponse(
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
    String executedFunds,
    Integer tradesCount,
    String preventedVolume,
    String preventedLocked
) {} 