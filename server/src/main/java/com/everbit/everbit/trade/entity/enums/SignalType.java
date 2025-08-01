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
    LOSS_MANAGEMENT("손실 관리"),
    STOCH_RSI_CROSS_BUY("Stoch RSI 크로스 매수"),
    STOCH_RSI_CROSS_SELL("Stoch RSI 크로스 매도"),
    BOLLINGER_MEAN_REVERSION_BUY("볼린저 밴드 평균 회귀 매수"),
    BOLLINGER_MEAN_REVERSION_SELL("볼린저 밴드 평균 회귀 매도"),
    UNKNOWN("알 수 없음");

    private final String description;
} 