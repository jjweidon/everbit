package com.everbit.everbit.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Strategy {
    EXTREME_FLIP(
        "극점포착",
        "연속 하락/상승 직후 정체 시 거래하는 전략",
        true,
        CandleInterval.SIXTY,
        100
    ),

    TRIPLE_INDICATOR_MODERATE(
        "3지표 중간전략", 
        "볼린저밴드, RSI, MACD 3가지 지표 중 2개 이상이 매수/매도 시그널을 보낼 때 거래하는 중간 전략",
        true,
        CandleInterval.FIVE,
        200
    ),

    // PASSIVE
    LOSS_MANAGEMENT(
        "손실관리",
        "손실관리를 위한 전략",
        false,
        CandleInterval.TEN,
        100
    ),

    PROFIT_TAKING(
        "이익실현",
        "이익실현을 위한 전략",
        false,
        CandleInterval.TEN,
        100
    ),
    
    TIMEOUT_SELL(
        "시간초과매도",
        "30분 경과 후 0.1% 이익도 못 내면 전량 매도하는 전략",
        false,
        CandleInterval.TEN,
        100
    );

    private final String value;
    private final String description;
    private final boolean userConfigurable;
    private final CandleInterval candleInterval;
    private final int candleCount;
}