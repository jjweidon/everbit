package com.everbit.everbit.upbit.dto.trading;

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
    boolean bbOverSold,
    boolean bbOverBought,
    boolean meanReversionBuySignal,
    boolean meanReversionSellSignal
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
        return meanReversionBuySignal;
    }
    
    public boolean isSellSignal() {
        return meanReversionSellSignal;
    }
    
    // 기존 평균 회귀 전략 (볼린저 밴드 + RSI)
    public boolean isMeanReversionBuySignal() {
        // 가격이 이동평균선 아래로 크게 떨어졌을 때 매수 (과매도 상황)
        return bbOverSold || rsiOversold;
    }
    
    public boolean isMeanReversionSellSignal() {
        // 가격이 이동평균선 위로 크게 올라갔을 때 매도 (과매수 상황)
        return bbOverBought || rsiOverbought;
    }
    
    // 새로운 평균 회귀 전략 (20캔들 전과 5분캔들 전후 비교)
    public boolean isNewMeanReversionBuySignal() {
        return meanReversionBuySignal;
    }
    
    public boolean isNewMeanReversionSellSignal() {
        return meanReversionSellSignal;
    }
} 