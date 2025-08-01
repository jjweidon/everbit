package com.everbit.everbit.upbit.dto.trading;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.ta4j.core.num.Num;

public record TradingSignal(
    String market,
    ZonedDateTime timestamp,
    Num currentPrice,
    boolean goldenCross,
    boolean deadCross,
    boolean macdBuySignal,
    boolean macdSellSignal,
    boolean rsiOversold,
    boolean rsiOverbought,
    BigDecimal rsiValue,
    boolean stochRsiOversold,
    boolean stochRsiOverbought,
    BigDecimal stochRsiKValue,  // StochRSI %K 값
    BigDecimal stochRsiDValue,  // StochRSI %D 값
    boolean bbOverSold,
    boolean bbOverBought,
    boolean bbMeanReversionBuySignal,   // 볼린저 밴드 평균 회귀 매수 시그널
    boolean bbMeanReversionSellSignal   // 볼린저 밴드 평균 회귀 매도 시그널
) {
    public boolean isEmaMomentumBuySignal() {
        return goldenCross && macdBuySignal;
    }
    public boolean isEmaMomentumSellSignal() {
        return deadCross && macdSellSignal;
    }
    
    public boolean isMacdRsiBuySignal() {
        return macdBuySignal && rsiOversold;
    }
    public boolean isMacdRsiSellSignal() {
        return macdSellSignal && rsiOverbought;
    }

    public boolean isBbMomentumBuySignal() {
        return bbOverSold && macdBuySignal;
    }
    public boolean isBbMomentumSellSignal() {
        return bbOverBought && macdSellSignal;
    }

    public boolean isGoldenCrossBuySignal() {
        return goldenCross;
    }
    public boolean isGoldenCrossSellSignal() {
        return deadCross;
    }

    public boolean isEnsembleBuySignal() {
        int cnt = 0;
        if (isEmaMomentumBuySignal()) cnt++;
        if (isMacdRsiBuySignal()) cnt++;
        if (isBbMomentumBuySignal()) cnt++;
        if (isGoldenCrossBuySignal()) cnt++;
        return cnt >= 2;
    }

    public boolean isEnsembleSellSignal() {
        int cnt = 0;
        if (isEmaMomentumSellSignal()) cnt++;
        if (isMacdRsiSellSignal()) cnt++;
        if (isBbMomentumSellSignal()) cnt++;
        if (isGoldenCrossSellSignal()) cnt++;
        return cnt >= 2;
    }

    // public boolean isBuySignal() {
    //     return isEnsembleBuySignal() || isEmaMomentumBuySignal() || isMacdRsiBuySignal() || isBbMomentumBuySignal() || isGoldenCrossBuySignal();
    // }
    
    // public boolean isSellSignal() {
    //     return isEnsembleSellSignal() || isEmaMomentumSellSignal() || isMacdRsiSellSignal() || isBbMomentumSellSignal() || isGoldenCrossSellSignal();
    // }

    public boolean isBuySignal() {
        return isStochRsiCrossBuySignal();
    }
    
    public boolean isSellSignal() {
        return isStochRsiCrossSellSignal();
    }
    
    /**
     * StochRSI %K와 %D 크로스오버 매수 시그널
     * %K가 %D를 상향 돌파할 때
     */
    public boolean isStochRsiCrossBuySignal() {
        return stochRsiOversold;
    }
    
    /**
     * StochRSI %K와 %D 크로스오버 매도 시그널
     * %K가 %D를 하향 돌파할 때
     */
    public boolean isStochRsiCrossSellSignal() {
        return stochRsiOverbought;
    }
    
    /**
     * 볼린저 밴드 평균 회귀 매수 시그널
     * 조건: 가격이 하단 밴드 터치 + RSI 과매도 + MACD 상승 신호
     */
    public boolean isBollingerMeanReversionBuySignal() {
        return bbMeanReversionBuySignal;
    }
    
    /**
     * 볼린저 밴드 평균 회귀 매도 시그널
     * 조건: 가격이 중간 밴드 도달 + RSI 과매수 + MACD 하락 신호
     */
    public boolean isBollingerMeanReversionSellSignal() {
        return bbMeanReversionSellSignal;
    }
    
    /**
     * 볼린저 밴드 평균 회귀 전략을 포함한 앙상블 매수 시그널
     */
    public boolean isEnhancedEnsembleBuySignal() {
        int cnt = 0;
        if (isEmaMomentumBuySignal()) cnt++;
        if (isMacdRsiBuySignal()) cnt++;
        if (isBbMomentumBuySignal()) cnt++;
        if (isGoldenCrossBuySignal()) cnt++;
        if (isBollingerMeanReversionBuySignal()) cnt++;
        return cnt >= 2;
    }
    
    /**
     * 볼린저 밴드 평균 회귀 전략을 포함한 앙상블 매도 시그널
     */
    public boolean isEnhancedEnsembleSellSignal() {
        int cnt = 0;
        if (isEmaMomentumSellSignal()) cnt++;
        if (isMacdRsiSellSignal()) cnt++;
        if (isBbMomentumSellSignal()) cnt++;
        if (isGoldenCrossSellSignal()) cnt++;
        if (isBollingerMeanReversionSellSignal()) cnt++;
        return cnt >= 2;
    }
}