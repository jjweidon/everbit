package com.everbit.everbit.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SignalType {
    LOSS_MANAGEMENT("손실 관리"),
    PROFIT_TAKING("이익 실현"),
    
    // Extreme Flip 전략
    DROP_N_FLIP("연속 하락 후 추세 전환 신호"),
    POP_N_FLIP("연속 상승 후 추세 전환 신호"),
    
    UNKNOWN("알 수 없음");

    private final String description;
} 