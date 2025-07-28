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
    boolean bbOverSold,
    boolean bbOverBought
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
        return rsiOversold || bbOverSold;
    }
    
    public boolean isSellSignal() {
        return rsiOverbought || bbOverBought;
    }
}