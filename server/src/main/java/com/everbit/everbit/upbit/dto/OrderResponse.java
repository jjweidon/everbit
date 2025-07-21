package com.everbit.everbit.upbit.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.OffsetDateTime;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OrderResponse(
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
    String executedFunds,
    Integer tradesCount,
    String preventedVolume,
    String preventedLocked,
    List<Trade> trades,  // trades는 개별 주문 조회에서만 사용됨
    String newOrderUuid  // cancel_and_new API 응답에서만 사용됨
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