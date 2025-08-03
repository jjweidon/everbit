package com.everbit.everbit.upbit.service;

import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.num.Num;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.trade.entity.enums.Strategy;
import com.everbit.everbit.upbit.dto.trading.TradingSignal;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class TradingSignalService {
    private final CandleDataService candleDataService;
    
    // 기술적 지표 계산을 위한 상수 정의
    private static final int RSI_PERIOD = 9;  // RSI 기간
    private static final int BB_PERIOD = 20;  // 볼린저 밴드 기간
    private static final int MACD_SHORT = 6;  // MACD 단기
    private static final int MACD_LONG = 13;  // MACD 장기
    private static final int MACD_SIGNAL = 5; // MACD 시그널
    
    // RSI 기준값
    private static final int RSI_OVERSOLD = 35;   // RSI 과매도 기준
    private static final int RSI_OVERBOUGHT = 65; // RSI 과매수 기준
    
    public TradingSignal calculateSignals(String market, User user) {
        BarSeries series = candleDataService.createBarSeries(market, user);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        
        int lastIndex = series.getEndIndex();
        
        // 개별 지표 시그널 계산
        boolean bbBuySignal = calculateBollingerBandsBuySignal(series, lastIndex);
        boolean bbSellSignal = calculateBollingerBandsSellSignal(series, lastIndex);
        boolean rsiBuySignal = calculateRSIBuySignal(series, lastIndex);
        boolean rsiSellSignal = calculateRSISellSignal(series, lastIndex);
        boolean macdBuySignal = calculateMACDBuySignal(series, lastIndex);
        boolean macdSellSignal = calculateMACDSellSignal(series, lastIndex);
        
        // 지표 값들 계산
        Num bbLowerBand = getBollingerBandsLower(series, lastIndex);
        Num bbMiddleBand = getBollingerBandsMiddle(series, lastIndex);
        Num bbUpperBand = getBollingerBandsUpper(series, lastIndex);
        Num rsiValue = getRSIValue(series, lastIndex);
        Num macdValue = getMACDValue(series, lastIndex);
        Num macdSignalValue = getMACDSignalValue(series, lastIndex);
        Num macdHistogram = getMACDHistogram(series, lastIndex);

        return new TradingSignal(
            market,
            series.getLastBar().getEndTime(),
            closePrice.getValue(lastIndex),
            bbBuySignal,
            bbSellSignal,
            rsiBuySignal,
            rsiSellSignal,
            macdBuySignal,
            macdSellSignal,
            bbLowerBand,
            bbMiddleBand,
            bbUpperBand,
            rsiValue,
            macdValue,
            macdSignalValue,
            macdHistogram
        );
    }

    /**
     * 시그널 강도에 따른 주문 금액 계산
     */
    public BigDecimal calculateOrderAmountBySignalStrength(TradingSignal signal, Strategy strategy, 
                                                          BigDecimal baseOrderAmount, BigDecimal maxOrderAmount) {
        // 시그널 강도 계산 (0.0 ~ 1.0)
        double signalStrength = calculateSignalStrength(signal, strategy);
        
        // 시그널 강도에 따른 주문 금액 계산
        BigDecimal strengthMultiplier = BigDecimal.valueOf(signalStrength);
        BigDecimal orderAmount = baseOrderAmount.add(
            maxOrderAmount.subtract(baseOrderAmount).multiply(strengthMultiplier)
        );
        
        return orderAmount.setScale(8, RoundingMode.HALF_UP);
    }
    
    /**
     * 선택된 전략의 시그널 강도를 계산합니다 (0.0 ~ 1.0)
     */
    public double calculateSignalStrength(TradingSignal signal, Strategy strategy) {
        switch (strategy) {
            case TRIPLE_INDICATOR_CONSERVATIVE:
                return calculateTripleIndicatorConservativeSignalStrength(signal);
            case TRIPLE_INDICATOR_MODERATE:
                return calculateTripleIndicatorModerateSignalStrength(signal);
            case TRIPLE_INDICATOR_AGGRESSIVE:
                return calculateTripleIndicatorAggressiveSignalStrength(signal);
            case BB_RSI_COMBO:
                return calculateBbRsiComboSignalStrength(signal);
            case RSI_MACD_COMBO:
                return calculateRsiMacdComboSignalStrength(signal);
            case BB_MACD_COMBO:
                return calculateBbMacdComboSignalStrength(signal);
            default:
                return 0.5; // 기본값
        }
    }
    
    /**
     * 3지표 보수전략 시그널 강도 계산
     */
    private double calculateTripleIndicatorConservativeSignalStrength(TradingSignal signal) {
        if (signal.isTripleIndicatorConservativeBuySignal() || signal.isTripleIndicatorConservativeSellSignal()) {
            // 3개 지표 모두 시그널이므로 최대 강도
            return 1.0;
        }
        return 0.0;
    }
    
    /**
     * 3지표 중간전략 시그널 강도 계산
     */
    private double calculateTripleIndicatorModerateSignalStrength(TradingSignal signal) {
        if (signal.isTripleIndicatorModerateBuySignal() || signal.isTripleIndicatorModerateSellSignal()) {
            int signalCount = signal.isTripleIndicatorModerateBuySignal() ? 
                signal.getBuySignalCount() : signal.getSellSignalCount();
            
            // 기본 강도: 시그널 개수에 따른 강도
            double baseStrength = signalCount / 3.0;
            
            // 지표별 세부 강도 계산
            double detailedStrength = calculateDetailedSignalStrength(signal, signal.isTripleIndicatorModerateBuySignal());
            
            // 기본 강도와 세부 강도의 평균
            return (baseStrength + detailedStrength) / 2.0;
        }
        return 0.0;
    }
    
    /**
     * 3지표 공격전략 시그널 강도 계산
     */
    private double calculateTripleIndicatorAggressiveSignalStrength(TradingSignal signal) {
        if (signal.isTripleIndicatorAggressiveBuySignal() || signal.isTripleIndicatorAggressiveSellSignal()) {
            int signalCount = signal.isTripleIndicatorAggressiveBuySignal() ? 
                signal.getBuySignalCount() : signal.getSellSignalCount();
            
            // 기본 강도: 시그널 개수에 따른 강도
            double baseStrength = signalCount / 3.0;
            
            // 지표별 세부 강도 계산
            double detailedStrength = calculateDetailedSignalStrength(signal, signal.isTripleIndicatorAggressiveBuySignal());
            
            // 기본 강도와 세부 강도의 평균
            return (baseStrength + detailedStrength) / 2.0;
        }
        return 0.0;
    }
    
    /**
     * 볼린저+RSI 조합 시그널 강도 계산
     */
    private double calculateBbRsiComboSignalStrength(TradingSignal signal) {
        if (signal.isBbRsiComboBuySignal() || signal.isBbRsiComboSellSignal()) {
            return calculateDetailedSignalStrength(signal, signal.isBbRsiComboBuySignal());
        }
        return 0.0;
    }
    
    /**
     * RSI+MACD 조합 시그널 강도 계산
     */
    private double calculateRsiMacdComboSignalStrength(TradingSignal signal) {
        if (signal.isRsiMacdComboBuySignal() || signal.isRsiMacdComboSellSignal()) {
            return calculateDetailedSignalStrength(signal, signal.isRsiMacdComboBuySignal());
        }
        return 0.0;
    }
    
    /**
     * 볼린저+MACD 조합 시그널 강도 계산
     */
    private double calculateBbMacdComboSignalStrength(TradingSignal signal) {
        if (signal.isBbMacdComboBuySignal() || signal.isBbMacdComboSellSignal()) {
            return calculateDetailedSignalStrength(signal, signal.isBbMacdComboBuySignal());
        }
        return 0.0;
    }
    
    /**
     * 지표 값들의 차이에 따른 세부 시그널 강도 계산 (0.0 ~ 1.0)
     */
    private double calculateDetailedSignalStrength(TradingSignal signal, boolean isBuySignal) {
        double bbStrength = calculateBollingerBandsStrength(signal, isBuySignal);
        double rsiStrength = calculateRSIStrength(signal, isBuySignal);
        double macdStrength = calculateMACDStrength(signal, isBuySignal);
        
        // 활성화된 지표들의 강도 평균 계산
        int activeIndicators = 0;
        double totalStrength = 0.0;
        
        if (signal.bbBuySignal() || signal.bbSellSignal()) {
            totalStrength += bbStrength;
            activeIndicators++;
        }
        if (signal.rsiBuySignal() || signal.rsiSellSignal()) {
            totalStrength += rsiStrength;
            activeIndicators++;
        }
        if (signal.macdBuySignal() || signal.macdSellSignal()) {
            totalStrength += macdStrength;
            activeIndicators++;
        }
        
        return activeIndicators > 0 ? totalStrength / activeIndicators : 0.0;
    }
    
    /**
     * 볼린저밴드 강도 계산
     * 매수: 현재가가 하단 밴드에서 얼마나 멀리 떨어져 있는지
     * 매도: 현재가가 상단 밴드에서 얼마나 멀리 떨어져 있는지
     */
    private double calculateBollingerBandsStrength(TradingSignal signal, boolean isBuySignal) {
        double currentPrice = signal.currentPrice().doubleValue();
        double bbLower = signal.bbLowerBand().doubleValue();
        double bbMiddle = signal.bbMiddleBand().doubleValue();
        double bbUpper = signal.bbUpperBand().doubleValue();
        
        if (isBuySignal) {
            // 매수: 현재가가 하단 밴드 아래에 있을 때
            if (currentPrice < bbLower) {
                // 하단 밴드와의 거리를 계산 (0.0 ~ 1.0)
                double bandWidth = bbMiddle - bbLower;
                double distance = bbLower - currentPrice;
                return Math.min(distance / bandWidth, 1.0);
            }
        } else {
            // 매도: 현재가가 상단 밴드 위에 있을 때
            if (currentPrice > bbUpper) {
                // 상단 밴드와의 거리를 계산 (0.0 ~ 1.0)
                double bandWidth = bbUpper - bbMiddle;
                double distance = currentPrice - bbUpper;
                return Math.min(distance / bandWidth, 1.0);
            }
        }
        
        return 0.0;
    }
    
    /**
     * RSI 강도 계산
     * 매수: RSI가 과매도 기준에서 얼마나 낮은지
     * 매도: RSI가 과매수 기준에서 얼마나 높은지
     */
    private double calculateRSIStrength(TradingSignal signal, boolean isBuySignal) {
        double rsiValue = signal.rsiValue().doubleValue();
        
        if (isBuySignal) {
            // 매수: RSI가 35 이하일 때
            if (rsiValue < RSI_OVERSOLD) {
                // RSI가 0에 가까울수록 강한 시그널 (0.0 ~ 1.0)
                return (RSI_OVERSOLD - rsiValue) / RSI_OVERSOLD;
            }
        } else {
            // 매도: RSI가 65 이상일 때
            if (rsiValue > RSI_OVERBOUGHT) {
                // RSI가 100에 가까울수록 강한 시그널 (0.0 ~ 1.0)
                return (rsiValue - RSI_OVERBOUGHT) / (100 - RSI_OVERBOUGHT);
            }
        }
        
        return 0.0;
    }
    
    /**
     * MACD 히스토그램 강도 계산
     * 매수: 히스토그램이 얼마나 증가했는지
     * 매도: 히스토그램이 얼마나 감소했는지
     */
    private double calculateMACDStrength(TradingSignal signal, boolean isBuySignal) {
        double macdValue = signal.macdValue().doubleValue();
        double histogram = signal.macdHistogram().doubleValue();
        
        // MACD 히스토그램의 절대값을 기준으로 강도 계산
        double absHistogram = Math.abs(histogram);
        
        // 히스토그램이 0에 가까우면 약한 시그널, 멀수록 강한 시그널
        // 일반적으로 MACD 히스토그램의 범위를 0.1% ~ 1%로 가정
        double maxExpectedHistogram = Math.abs(macdValue) * 0.01; // MACD 값의 1%를 최대값으로 가정
        
        if (maxExpectedHistogram > 0) {
            return Math.min(absHistogram / maxExpectedHistogram, 1.0);
        }
        
        return 0.5; // 기본값
    }
    
    // ==================== 개별 지표 시그널 계산 메서드들 ====================
    
    /**
     * 볼린저밴드 매수 시그널 계산
     * 조건: 현재가 < BB 하단
     */
    private boolean calculateBollingerBandsBuySignal(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        Num currentPrice = closePrice.getValue(index);
        Num bbLower = getBollingerBandsLower(series, index);
        
        return currentPrice.isLessThan(bbLower);
    }
    
    /**
     * 볼린저밴드 매도 시그널 계산
     * 조건: 현재가 > BB 중단선 or 상단선
     */
    private boolean calculateBollingerBandsSellSignal(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        Num currentPrice = closePrice.getValue(index);
        Num bbMiddle = getBollingerBandsMiddle(series, index);
        Num bbUpper = getBollingerBandsUpper(series, index);
        
        return currentPrice.isGreaterThan(bbMiddle) || currentPrice.isGreaterThan(bbUpper);
    }
    
    /**
     * RSI 매수 시그널 계산
     * 조건: RSI(9) < 35
     */
    private boolean calculateRSIBuySignal(BarSeries series, int index) {
        RSIIndicator rsi = new RSIIndicator(new ClosePriceIndicator(series), RSI_PERIOD);
        Num rsiValue = rsi.getValue(index);
        
        return rsiValue.isLessThan(series.numOf(RSI_OVERSOLD));
    }
    
    /**
     * RSI 매도 시그널 계산
     * 조건: RSI > 65
     */
    private boolean calculateRSISellSignal(BarSeries series, int index) {
        RSIIndicator rsi = new RSIIndicator(new ClosePriceIndicator(series), RSI_PERIOD);
        Num rsiValue = rsi.getValue(index);
        
        return rsiValue.isGreaterThan(series.numOf(RSI_OVERBOUGHT));
    }
    
    /**
     * MACD 매수 시그널 계산
     * 조건: MACD 히스토그램 ↑ 전환 (이전보다 증가)
     */
    private boolean calculateMACDBuySignal(BarSeries series, int index) {
        if (index <= 0) return false;
        
        Num currentHistogram = getMACDHistogram(series, index);
        Num previousHistogram = getMACDHistogram(series, index - 1);
        
        return currentHistogram.isGreaterThan(previousHistogram);
    }
    
    /**
     * MACD 매도 시그널 계산
     * 조건: MACD 히스토그램 하락 전환 (이전보다 감소)
     */
    private boolean calculateMACDSellSignal(BarSeries series, int index) {
        if (index <= 0) return false;
        
        Num currentHistogram = getMACDHistogram(series, index);
        Num previousHistogram = getMACDHistogram(series, index - 1);
        
        return currentHistogram.isLessThan(previousHistogram);
    }
    
    // ==================== 지표 값 계산 헬퍼 메서드들 ====================
    
    private Num getBollingerBandsLower(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator smaBB = new SMAIndicator(closePrice, BB_PERIOD);
        StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, BB_PERIOD);
        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(smaBB);
        BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, sd);
        
        return bbl.getValue(index);
    }
    
    private Num getBollingerBandsMiddle(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator smaBB = new SMAIndicator(closePrice, BB_PERIOD);
        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(smaBB);
        
        return bbm.getValue(index);
    }
    
    private Num getBollingerBandsUpper(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator smaBB = new SMAIndicator(closePrice, BB_PERIOD);
        StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, BB_PERIOD);
        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(smaBB);
        BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, sd);
        
        return bbu.getValue(index);
    }
    
    private Num getRSIValue(BarSeries series, int index) {
        RSIIndicator rsi = new RSIIndicator(new ClosePriceIndicator(series), RSI_PERIOD);
        return rsi.getValue(index);
    }
    
    private Num getMACDValue(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
        return macd.getValue(index);
    }
    
    private Num getMACDSignalValue(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
        SMAIndicator signal = new SMAIndicator(macd, MACD_SIGNAL);
        return signal.getValue(index);
    }
    
    private Num getMACDHistogram(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
        SMAIndicator signal = new SMAIndicator(macd, MACD_SIGNAL);
        
        return macd.getValue(index).minus(signal.getValue(index));
    }
} 