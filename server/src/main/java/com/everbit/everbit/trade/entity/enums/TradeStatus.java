package com.everbit.everbit.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TradeStatus {
    WAIT("wait"),
    WATCH("watch"),
    DONE("done"),
    CANCEL("cancel");

    private final String value;
} 