package com.everbit.everbit.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SignalType {
    LOSS_MANAGEMENT("손실 관리"),
    PROFIT_TAKING("이익 실현"),
    
    // 3지표 보수전략
    TRIPLE_INDICATOR_CONSERVATIVE_BUY("3지표 보수전략 매수"),
    TRIPLE_INDICATOR_CONSERVATIVE_SELL("3지표 보수전략 매도"),
    
    // 3지표 중간전략
    TRIPLE_INDICATOR_MODERATE_BUY("3지표 중간전략 매수"),
    TRIPLE_INDICATOR_MODERATE_SELL("3지표 중간전략 매도"),
    
    // 3지표 공격전략
    TRIPLE_INDICATOR_AGGRESSIVE_BUY("3지표 공격전략 매수"),
    TRIPLE_INDICATOR_AGGRESSIVE_SELL("3지표 공격전략 매도"),
    
    // 2지표 조합 전략들
    BB_RSI_COMBO_BUY("볼린저+RSI 조합 매수"),
    BB_RSI_COMBO_SELL("볼린저+RSI 조합 매도"),
    RSI_MACD_COMBO_BUY("RSI+MACD 조합 매수"),
    RSI_MACD_COMBO_SELL("RSI+MACD 조합 매도"),
    BB_MACD_COMBO_BUY("볼린저+MACD 조합 매수"),
    BB_MACD_COMBO_SELL("볼린저+MACD 조합 매도"),

    // 커스텀 시그널
    DROP_N_FLIP("연속 하락 후 추세 전환 신호"),
    
    UNKNOWN("알 수 없음");

    private final String description;
} 