package com.everbit.everbit.upbit.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.OffsetDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ReplaceOrderResponse(
    String uuid,
    String side,
    String ordType,
    String price,
    String state,
    String market,
    OffsetDateTime createdAt,
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
    String newOrderUuid
) {} 