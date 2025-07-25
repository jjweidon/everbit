package com.everbit.everbit.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Strategy {
    EMA_MOMENTUM("EMA 크로스 + 모멘텀", "EMA(9) vs EMA(21) 크로스와 ADX 모멘텀 필터를 결합한 전략", 2),  // 2분
    MACD_RSI("MACD + RSI 다중조건", "MACD 크로스와 RSI 과매수/과매도 필터를 결합한 전략", 2),          // 2분
    BB_MOMENTUM("볼린저밴드 + 모멘텀", "볼린저밴드 평균회귀와 모멘텀 필터를 결합한 전략", 2),          // 2분
    GOLDEN_CROSS("골든크로스 (50/200 EMA)", "50일 EMA와 200일 EMA의 크로스를 이용한 장기 추세추종 전략", 15),  // 15분
    ENSEMBLE("앙상블 전략", "여러 전략의 시그널을 종합적으로 분석하여 매매하는 전략", 3);              // 3분

    private final String name;
    private final String description;
    private final int interval;

    public long getIntervalMillis() {
        return interval * 60000L;
    }

    public long getIntervalSeconds() {
        return interval * 60L;
    }
}