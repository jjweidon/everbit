package com.everbit.everbit.upbit.dto.trading;

import java.time.ZonedDateTime;
import org.ta4j.core.num.Num;

public record TradingSignal(
    String market,
    ZonedDateTime timestamp,
    Num currentPrice,
    
    // 개별 지표 시그널
    boolean bbBuySignal,      // 볼린저밴드 매수 시그널
    boolean bbSellSignal,     // 볼린저밴드 매도 시그널
    boolean rsiBuySignal,     // RSI 매수 시그널  
    boolean rsiSellSignal,    // RSI 매도 시그널
    boolean macdBuySignal,    // MACD 매수 시그널
    boolean macdSellSignal,   // MACD 매도 시그널
    
    // DROP_N_FLIP과 POP_N_FLIP 시그널
    boolean dropNFlipBuySignal,   // DROP_N_FLIP 매수 시그널
    boolean popNFlipSellSignal,   // POP_N_FLIP 매도 시그널
    double dropNFlipStrength,     // DROP_N_FLIP 시그널 강도
    double popNFlipStrength,      // POP_N_FLIP 시그널 강도
    
    // 지표 값들 (디버깅 및 강도 계산용)
    Num bbLowerBand,          // 볼린저밴드 하단
    Num bbMiddleBand,         // 볼린저밴드 중간
    Num bbUpperBand,          // 볼린저밴드 상단
    Num rsiValue,             // RSI 값
    Num macdValue,            // MACD 값
    Num macdSignalValue,      // MACD 시그널 값
    Num macdHistogram        // MACD 히스토그램
) {
    
    /**
     * 3지표 보수전략 매수 시그널
     * 조건: 3가지 지표가 모두 매수 시그널을 보낼 때
     */
    public boolean isTripleIndicatorConservativeBuySignal() {
        return bbBuySignal && rsiBuySignal && macdBuySignal;
    }
    
    /**
     * 3지표 보수전략 매도 시그널
     */
    public boolean isTripleIndicatorConservativeSellSignal() {
        return bbSellSignal && rsiSellSignal && macdSellSignal;
    }
    
    /**
     * 3지표 중간전략 매수 시그널
     * 조건: 3가지 지표 중 2개 이상이 매수 시그널을 보낼 때
     */
    public boolean isTripleIndicatorModerateBuySignal() {
        int buyCount = 0;
        if (bbBuySignal) buyCount++;
        if (rsiBuySignal) buyCount++;
        if (macdBuySignal) buyCount++;
        return buyCount >= 2;
    }
    
    /**
     * 3지표 중간전략 매도 시그널
     */
    public boolean isTripleIndicatorModerateSellSignal() {
        int sellCount = 0;
        if (bbSellSignal) sellCount++;
        if (rsiSellSignal) sellCount++;
        if (macdSellSignal) sellCount++;
        return sellCount >= 2;
    }
    
    /**
     * 3지표 공격전략 매수 시그널
     * 조건: 3가지 지표 중 1개라도 매수 시그널을 보낼 때
     */
    public boolean isTripleIndicatorAggressiveBuySignal() {
        return bbBuySignal || rsiBuySignal || macdBuySignal;
    }
    
    /**
     * 3지표 공격전략 매도 시그널
     */
    public boolean isTripleIndicatorAggressiveSellSignal() {
        return bbSellSignal || rsiSellSignal || macdSellSignal;
    }
    
    /**
     * 볼린저+RSI 조합 매수 시그널
     */
    public boolean isBbRsiComboBuySignal() {
        return bbBuySignal && rsiBuySignal;
    }
    
    /**
     * 볼린저+RSI 조합 매도 시그널
     */
    public boolean isBbRsiComboSellSignal() {
        return bbSellSignal && rsiSellSignal;
    }
    
    /**
     * RSI+MACD 조합 매수 시그널
     */
    public boolean isRsiMacdComboBuySignal() {
        return rsiBuySignal && macdBuySignal;
    }
    
    /**
     * RSI+MACD 조합 매도 시그널
     */
    public boolean isRsiMacdComboSellSignal() {
        return rsiSellSignal && macdSellSignal;
    }
    
    /**
     * 볼린저+MACD 조합 매수 시그널
     */
    public boolean isBbMacdComboBuySignal() {
        return bbBuySignal && macdBuySignal;
    }
    
    /**
     * 볼린저+MACD 조합 매도 시그널
     */
    public boolean isBbMacdComboSellSignal() {
        return bbSellSignal && macdSellSignal;
    }
    
    /**
     * 매수 시그널 개수 반환
     */
    public int getBuySignalCount() {
        int count = 0;
        if (bbBuySignal) count++;
        if (rsiBuySignal) count++;
        if (macdBuySignal) count++;
        return count;
    }
    
    /**
     * 매도 시그널 개수 반환
     */
    public int getSellSignalCount() {
        int count = 0;
        if (bbSellSignal) count++;
        if (rsiSellSignal) count++;
        if (macdSellSignal) count++;
        return count;
    }
    
    /**
     * 시그널 강도 계산 (0.0 ~ 1.0)
     * 시그널이 많을수록 강도가 높아짐
     */
    public double getSignalStrength(boolean isBuy) {
        int signalCount = isBuy ? getBuySignalCount() : getSellSignalCount();
        return signalCount / 3.0; // 3개 지표 기준으로 정규화
    }
}