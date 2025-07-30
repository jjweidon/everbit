package com.everbit.everbit.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Strategy {
    STOCH_RSI("Stoch RSI", "Stoch RSI를 이용한 전략"),
    RSI_BB("RSI + 볼린저밴드", "RSI와 볼린저밴드를 결합한 전략"),
    EMA_MOMENTUM("EMA 크로스 + 모멘텀", "EMA(9) vs EMA(21) 크로스와 ADX 모멘텀 필터를 결합한 전략"),
    BB_MOMENTUM("볼린저밴드 + 모멘텀", "볼린저밴드 평균회귀와 모멘텀 필터를 결합한 전략"),
    GOLDEN_CROSS("골든크로스", "50일 EMA와 200일 EMA의 크로스를 이용한 장기 추세추종 전략"),
    ENSEMBLE("앙상블 전략", "여러 전략의 시그널을 종합적으로 분석하여 매매하는 전략");

    private final String name;
    private final String description;
}