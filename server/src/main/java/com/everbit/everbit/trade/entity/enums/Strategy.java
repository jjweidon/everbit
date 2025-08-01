package com.everbit.everbit.trade.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Strategy {
    BOLLINGER_MEAN_REVERSION(
        "볼린저 평균회귀",
        "가격이 볼린저 밴드 하단을 터치하고 과매도(RSI) 상태일 때, 반등을 노리는 평균 회귀 전략",
        true
    ),

    BB_MOMENTUM(
        "볼린저 + 모멘텀",
        "가격이 볼린저 밴드 수렴 구간에서 이탈할 때, 모멘텀 지표로 추세 방향을 판단하여 진입하는 전략",
        true
    ),

    EMA_MOMENTUM(
        "EMA 모멘텀",
        "단기/중기 이동평균(EMA 9/21)의 교차와 ADX로 추세 강도를 판단해 추세에 진입하는 전략",
        true
    ),

    ENSEMBLE(
        "앙상블",
        "여러 개별 전략의 매수/매도 시그널을 조합하여 신뢰도 높은 매매 타이밍을 포착하는 전략",
        true
    ),

    ENHANCED_ENSEMBLE(
        "강화 앙상블",
        "볼린저 평균회귀 등 복수 전략을 통합 분석해 더 정교하게 매매 타이밍을 결정하는 전략",
        true
    ),

    // PASSIVE
    LOSS_MANAGEMENT(
        "손실관리",
        "손실관리를 위한 전략",
        false
    );

    private final String value;
    private final String description;
    private final boolean userConfigurable;
}