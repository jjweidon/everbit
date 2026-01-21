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
    Num previousRSIValue,     // 이전 RSI 값 (RSI 크로스 확인용)
    Num macdValue,            // MACD 값
    Num macdSignalValue,      // MACD 시그널 값
    Num macdHistogram,        // MACD 히스토그램
    
    // 추세 및 변동성 지표 (Extreme Flip 전략 개선용)
    Num adxValue,             // ADX 값 (추세 강도)
    Num plusDI,               // +DI 값
    Num minusDI,              // -DI 값
    Num ema20,                // EMA 20
    Num ema60,                // EMA 60
    Num ema120,               // EMA 120
    Num atrValue              // ATR 값 (변동성)
) {
    // 지표 값들은 유지하되, 다른 전략 관련 메서드는 제거하고 EXTREME_FLIP 전략에만 집중
}