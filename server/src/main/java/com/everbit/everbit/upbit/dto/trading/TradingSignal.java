package com.everbit.everbit.upbit.dto.trading;

import java.time.ZonedDateTime;
import org.ta4j.core.num.Num;
import com.everbit.everbit.trade.entity.enums.Strategy;

public record TradingSignal(
    String market,
    ZonedDateTime timestamp,
    Num currentPrice,
    
    // EMA 크로스 시그널
    boolean goldenCross,      // 단기/중기 EMA 크로스
    boolean deadCross,
    boolean ema50Above200,    // 50일선이 200일선 위에 있음
    boolean ema50Below200,    // 50일선이 200일선 아래에 있음
    boolean ema50Cross200Up,  // 50일선이 200일선을 상향돌파
    boolean ema50Cross200Down,// 50일선이 200일선을 하향돌파
    
    // MACD 시그널
    boolean macdBuySignal,
    boolean macdSellSignal,
    
    // RSI 시그널
    boolean rsiOversold,
    boolean rsiOverbought,
    
    // 볼린저밴드 시그널
    boolean bbOverSold,
    boolean bbOverBought,
    boolean priceAboveBBMiddle,  // 가격이 중앙선 위에 있음
    boolean priceBelowBBMiddle,  // 가격이 중앙선 아래에 있음
    boolean bbMiddleTrendUp,     // 중앙선 상승추세
    boolean bbMiddleTrendDown    // 중앙선 하락추세
) {
    /**
     * EMA + MACD 전략의 매수 시그널
     */
    public boolean isEmaMomentumBuySignal() {
        return goldenCross && macdBuySignal && priceAboveBBMiddle && bbMiddleTrendUp;
    }

    /**
     * EMA + MACD 전략의 매도 시그널
     */
    public boolean isEmaMomentumSellSignal() {
        return deadCross && macdSellSignal && priceBelowBBMiddle && bbMiddleTrendDown;
    }

    /**
     * MACD + RSI 전략의 매수 시그널
     */
    public boolean isMacdRsiBuySignal() {
        return macdBuySignal && rsiOversold && priceBelowBBMiddle;
    }

    /**
     * MACD + RSI 전략의 매도 시그널
     */
    public boolean isMacdRsiSellSignal() {
        return macdSellSignal && rsiOverbought && priceAboveBBMiddle;
    }

    /**
     * 볼린저밴드 + RSI 전략의 매수 시그널
     */
    public boolean isBBMomentumBuySignal() {
        return bbOverSold && rsiOversold && bbMiddleTrendUp;
    }

    /**
     * 볼린저밴드 + RSI 전략의 매도 시그널
     */
    public boolean isBBMomentumSellSignal() {
        return bbOverBought && rsiOverbought && bbMiddleTrendDown;
    }

    /**
     * 골든크로스 전략의 매수 시그널
     */
    public boolean isGoldenCrossSignal() {
        return ema50Cross200Up && priceAboveBBMiddle && !rsiOverbought;
    }

    /**
     * 데드크로스 전략의 매도 시그널
     */
    public boolean isDeadCrossSignal() {
        return ema50Cross200Down && priceBelowBBMiddle && !rsiOversold;
    }

    /**
     * 앙상블 전략의 매수 시그널
     * - 여러 전략의 매수 시그널이 동시에 발생
     * - 추세와 모멘텀이 일치
     */
    public boolean isEnsembleBuySignal() {
        // 최소 2개 이상의 매수 시그널
        int buySignals = 0;
        if (isEmaMomentumBuySignal()) buySignals++;
        if (isMacdRsiBuySignal()) buySignals++;
        if (isBBMomentumBuySignal()) buySignals++;
        if (isGoldenCrossSignal()) buySignals++;

        // 전체적인 상승 추세 확인
        boolean upTrend = ema50Above200 && bbMiddleTrendUp && priceAboveBBMiddle;
        
        return buySignals >= 2 && upTrend;
    }

    /**
     * 앙상블 전략의 매도 시그널
     * - 여러 전략의 매도 시그널이 동시에 발생
     * - 추세와 모멘텀이 일치
     */
    public boolean isEnsembleSellSignal() {
        // 최소 2개 이상의 매도 시그널
        int sellSignals = 0;
        if (isEmaMomentumSellSignal()) sellSignals++;
        if (isMacdRsiSellSignal()) sellSignals++;
        if (isBBMomentumSellSignal()) sellSignals++;
        if (isDeadCrossSignal()) sellSignals++;

        // 전체적인 하락 추세 확인
        boolean downTrend = ema50Below200 && bbMiddleTrendDown && priceBelowBBMiddle;
        
        return sellSignals >= 2 && downTrend;
    }

    /**
     * 현재 신호에 해당하는 전략 반환
     */
    public Strategy determineStrategy() {
        if (isGoldenCrossSignal() || isDeadCrossSignal()) {
            return Strategy.GOLDEN_CROSS;
        }
        if (isEmaMomentumBuySignal() || isEmaMomentumSellSignal()) {
            return Strategy.EMA_MOMENTUM;
        }
        if (isMacdRsiBuySignal() || isMacdRsiSellSignal()) {
            return Strategy.MACD_RSI;
        }
        if (isBBMomentumBuySignal() || isBBMomentumSellSignal()) {
            return Strategy.BB_MOMENTUM;
        }
        if (isEnsembleBuySignal() || isEnsembleSellSignal()) {
            return Strategy.ENSEMBLE;
        }
        return Strategy.ENSEMBLE; // 기본값
    }

    /**
     * 매수 시그널 여부
     */
    public boolean isBuySignal() {
        return isEmaMomentumBuySignal() || 
               isMacdRsiBuySignal() || 
               isBBMomentumBuySignal() || 
               isGoldenCrossSignal() ||
               isEnsembleBuySignal();
    }

    /**
     * 매도 시그널 여부
     */
    public boolean isSellSignal() {
        return isEmaMomentumSellSignal() || 
               isMacdRsiSellSignal() || 
               isBBMomentumSellSignal() || 
               isDeadCrossSignal() ||
               isEnsembleSellSignal();
    }
} 