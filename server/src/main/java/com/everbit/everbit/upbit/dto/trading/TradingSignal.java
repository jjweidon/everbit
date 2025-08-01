package com.everbit.everbit.upbit.dto.trading;

import java.time.ZonedDateTime;
import org.ta4j.core.num.Num;

public record TradingSignal(
    String market,
    ZonedDateTime timestamp,
    Num currentPrice,
    boolean bbMeanReversionBuySignal,   // 볼린저 밴드 평균 회귀 매수 시그널
    boolean bbMeanReversionSellSignal,  // 볼린저 밴드 평균 회귀 매도 시그널
    boolean bbMomentumBuySignal,        // 볼린저 밴드 + 모멘텀 매수 시그널
    boolean bbMomentumSellSignal,       // 볼린저 밴드 + 모멘텀 매도 시그널
    boolean emaMomentumBuySignal,       // EMA 모멘텀 매수 시그널
    boolean emaMomentumSellSignal       // EMA 모멘텀 매도 시그널
) {
    
    /**
     * 볼린저 평균회귀 전략 매수 시그널
     * 조건: 가격이 볼린저 밴드 하단을 터치하고 과매도(RSI) 상태일 때, 반등을 노리는 평균 회귀 전략
     */
    public boolean isBollingerMeanReversionBuySignal() {
        return bbMeanReversionBuySignal;
    }
    
    /**
     * 볼린저 평균회귀 전략 매도 시그널
     */
    public boolean isBollingerMeanReversionSellSignal() {
        return bbMeanReversionSellSignal;
    }
    
    /**
     * 볼린저 + 모멘텀 전략 매수 시그널
     * 조건: 가격이 볼린저 밴드 수렴 구간에서 이탈할 때, 모멘텀 지표로 추세 방향을 판단하여 진입하는 전략
     */
    public boolean isBbMomentumBuySignal() {
        return bbMomentumBuySignal;
    }
    
    /**
     * 볼린저 + 모멘텀 전략 매도 시그널
     */
    public boolean isBbMomentumSellSignal() {
        return bbMomentumSellSignal;
    }
    
    /**
     * EMA 모멘텀 전략 매수 시그널
     * 조건: 단기/중기 이동평균(EMA 9/21)의 교차와 MACD로 추세를 판단해 추세에 진입하는 전략
     */
    public boolean isEmaMomentumBuySignal() {
        return emaMomentumBuySignal;
    }
    
    /**
     * EMA 모멘텀 전략 매도 시그널
     */
    public boolean isEmaMomentumSellSignal() {
        return emaMomentumSellSignal;
    }
    
    /**
     * 앙상블 전략 매수 시그널
     * 조건: 여러 개별 전략의 매수/매도 시그널을 조합하여 신뢰도 높은 매매 타이밍을 포착하는 전략
     */
    public boolean isEnsembleBuySignal() {
        int cnt = 0;
        if (isEmaMomentumBuySignal()) cnt++;
        if (isBbMomentumBuySignal()) cnt++;
        return cnt >= 2;
    }

    /**
     * 앙상블 전략 매도 시그널
     */
    public boolean isEnsembleSellSignal() {
        int cnt = 0;
        if (isEmaMomentumSellSignal()) cnt++;
        if (isBbMomentumSellSignal()) cnt++;
        return cnt >= 2;
    }
    
    /**
     * 강화 앙상블 전략 매수 시그널
     * 조건: 볼린저 평균회귀 등 복수 전략을 통합 분석해 더 정교하게 매매 타이밍을 결정하는 전략
     */
    public boolean isEnhancedEnsembleBuySignal() {
        int cnt = 0;
        if (isEmaMomentumBuySignal()) cnt++;
        if (isBbMomentumBuySignal()) cnt++;
        if (isBollingerMeanReversionBuySignal()) cnt++;
        return cnt >= 2;
    }
    
    /**
     * 강화 앙상블 전략 매도 시그널
     */
    public boolean isEnhancedEnsembleSellSignal() {
        int cnt = 0;
        if (isEmaMomentumSellSignal()) cnt++;
        if (isBbMomentumSellSignal()) cnt++;
        if (isBollingerMeanReversionSellSignal()) cnt++;
        return cnt >= 2;
    }
}