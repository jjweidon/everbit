package com.everbit.everbit.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TradeStatus {
    PENDING("대기"),
    COMPLETED("완료"),
    CANCELED("취소"),
    FAILED("실패");

    private final String description;
} 