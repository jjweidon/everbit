package com.everbit.everbit.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SignalType {
    LOSS_MANAGEMENT("손실 관리"),
    BOLLINGER_MEAN_REVERSION_BUY("볼린저 밴드 평균 회귀 매수"),
    BOLLINGER_MEAN_REVERSION_SELL("볼린저 밴드 평균 회귀 매도"),
    BB_MOMENTUM_BUY("볼린저 + 모멘텀 매수"),
    BB_MOMENTUM_SELL("볼린저 + 모멘텀 매도"),
    EMA_MOMENTUM_BUY("EMA 모멘텀 매수"),
    EMA_MOMENTUM_SELL("EMA 모멘텀 매도"),
    ENSEMBLE_BUY("앙상블 매수"),
    ENSEMBLE_SELL("앙상블 매도"),
    ENHANCED_ENSEMBLE_BUY("강화 앙상블 매수"),
    ENHANCED_ENSEMBLE_SELL("강화 앙상블 매도"),
    UNKNOWN("알 수 없음");

    private final String description;
} 