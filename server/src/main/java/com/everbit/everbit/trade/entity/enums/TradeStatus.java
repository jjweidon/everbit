package com.everbit.everbit.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TradeStatus {
    WAIT("대기"),
    WATCH("관찰"),
    DONE("완료"),
    CANCEL("취소");

    private final String description;
} 