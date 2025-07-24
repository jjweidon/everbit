package com.everbit.everbit.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Strategy {
    MOMENTUM("모멘텀 전략", "가격 모멘텀을 기반으로 한 매매 전략"),
    EMA("EMA 크로스", "이동평균선 크로스를 활용한 매매 전략"),
    RSI("RSI 과매수/과매도", "RSI 지표를 활용한 과매수/과매도 매매 전략"),
    MACD("MACD 크로스", "MACD 지표를 활용한 매매 전략"),
    BOLLINGER_BANDS("볼린저밴드 과매수/과매도", "볼린저밴드 지표를 활용한 과매수/과매도 매매 전략");

    private final String name;
    private final String description;
}
