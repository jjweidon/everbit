package com.everbit.everbit.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SignalType {
    LOSS_MANAGEMENT("손실 관리"),
    PROFIT_TAKING("이익 실현"),
    
    // 3지표 중간전략
    TRIPLE_INDICATOR_MODERATE_BUY("3지표 중간전략 매수"),
    TRIPLE_INDICATOR_MODERATE_SELL("3지표 중간전략 매도"),

    // 커스텀 시그널
    DROP_N_FLIP("연속 하락 후 추세 전환 신호"),
    POP_N_FLIP("연속 상승 후 추세 전환 신호"),
    
    UNKNOWN("알 수 없음");

    private final String description;
} 