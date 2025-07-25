package com.everbit.everbit.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SignalType {
    GOLDEN_CROSS("골든크로스"),
    MACD_BUY("MACD 매수"),
    RSI_OVERSOLD("RSI 과매도"),
    BB_OVERSOLD("볼린저밴드 과매도"),
    DEAD_CROSS("데드크로스"),
    MACD_SELL("MACD 매도"),
    RSI_OVERBOUGHT("RSI 과매수"),
    BB_OVERBOUGHT("볼린저밴드 과매수"),
    UNKNOWN("알 수 없음");

    private final String description;
} 