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
    boolean bbOverBought
) {
    public boolean isBuySignal() {
        return goldenCross || macdBuySignal || rsiOversold || bbOverSold;
    }
    
    public boolean isSellSignal() {
        return deadCross || macdSellSignal || rsiOverbought || bbOverBought;
    }
} 