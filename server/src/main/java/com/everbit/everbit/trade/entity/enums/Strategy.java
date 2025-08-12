package com.everbit.everbit.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Strategy {
    STANDARD(
        "에버비트 스탠다드",
        "연속 상승 / 연속 하락 후 추세 전환 신호가 오면 거래하는 전략",
        true,
        CandleInterval.FIVE,
        200
    ),

    TRIPLE_INDICATOR_MODERATE(
        "3지표 중간전략", 
        "볼린저밴드, RSI, MACD 3가지 지표 중 2개 이상이 매수/매도 시그널을 보낼 때 거래하는 중간 전략",
        true,
        CandleInterval.FIVE,
        200
    ),

    TRIPLE_INDICATOR_CONSERVATIVE(
        "3지표 보수전략",
        "볼린저밴드, RSI, MACD 3가지 지표가 모두 매수/매도 시그널을 보낼 때만 거래하는 보수적 전략",
        true,
        CandleInterval.FIVE,
        200
    ),

    TRIPLE_INDICATOR_AGGRESSIVE(
        "3지표 공격전략",
        "볼린저밴드, RSI, MACD 3가지 지표 중 1개라도 매수/매도 시그널을 보내면 거래하는 공격적 전략", 
        true,
        CandleInterval.FIVE,
        200
    ),

    BB_RSI_COMBO(
        "볼린저+RSI 조합",
        "볼린저밴드와 RSI 지표만을 조합한 전략",
        true,
        CandleInterval.TEN,
        100
    ),

    RSI_MACD_COMBO(
        "RSI+MACD 조합",
        "RSI와 MACD 지표만을 조합한 전략",
        true,
        CandleInterval.TEN,
        100
    ),

    BB_MACD_COMBO(
        "볼린저+MACD 조합", 
        "볼린저밴드와 MACD 지표만을 조합한 전략",
        true,
        CandleInterval.TEN,
        100
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