package com.everbit.everbit.upbit.service;

import com.everbit.everbit.trade.entity.enums.Strategy;
import com.everbit.everbit.upbit.dto.trading.TradingSignal;
import com.everbit.everbit.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradingSignalService {
    private final CandleDataService candleDataService;
    
    // 기술적 지표 계산을 위한 상수 정의
    private static final int RSI_PERIOD = 9;  // RSI 기간
    private static final int BB_PERIOD = 20;  // 볼린저 밴드 기간
    private static final int MACD_SHORT = 6;  // MACD 단기
    private static final int MACD_LONG = 13;  // MACD 장기
    private static final int MACD_SIGNAL = 5; // MACD 시그널
    
    // RSI 기준값
    private static final int RSI_OVERSOLD = 30;   // RSI 과매도 기준
    private static final int RSI_OVERBOUGHT = 70; // RSI 과매수 기준
    
    // 볼린저밴드 기준값
    private static final double BB_LOWER_THRESHOLD = 0.02; // BB 하단 대비 2% 이내에서 매수
    private static final double BB_UPPER_THRESHOLD = 0.98; // BB 상단 대비 98% 이상에서 매도
    
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
        
        // 매수/매도 시그널 충돌 해결: 더 강한 시그널 우선
        if (bbBuySignal && bbSellSignal) {
            // 볼린저밴드의 경우 현재가 위치에 따라 결정
            Num currentPrice = closePrice.getValue(lastIndex);
            Num bbMiddle = getBollingerBandsMiddle(series, lastIndex);
            bbSellSignal = currentPrice.isGreaterThan(bbMiddle);
            bbBuySignal = !bbSellSignal;
        }
        
        if (rsiBuySignal && rsiSellSignal) {
            // RSI의 경우 과매도/과매수 구간에 따라 결정
            Num rsiValue = getRSIValue(series, lastIndex);
            rsiSellSignal = rsiValue.isGreaterThan(series.numOf(50));
            rsiBuySignal = !rsiSellSignal;
        }
        
        if (macdBuySignal && macdSellSignal) {
            // MACD의 경우 MACD 값의 부호에 따라 결정
            Num macdValue = getMACDValue(series, lastIndex);
            macdSellSignal = macdValue.isLessThan(series.numOf(0));
            macdBuySignal = !macdSellSignal;
        }
        
        // 지표 값들 계산
        Num bbLowerBand = getBollingerBandsLower(series, lastIndex);
        Num bbMiddleBand = getBollingerBandsMiddle(series, lastIndex);
        Num bbUpperBand = getBollingerBandsUpper(series, lastIndex);
        Num rsiValue = getRSIValue(series, lastIndex);
        Num macdValue = getMACDValue(series, lastIndex);
        Num macdSignalValue = getMACDSignalValue(series, lastIndex);
        Num macdHistogram = getMACDHistogram(series, lastIndex);

        TradingSignal signal = new TradingSignal(
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
        
        // 시그널 생성 결과 로깅
        log.debug("마켓: {} - 시그널 생성 완료 [BB: 매수={}, 매도={}, RSI: 매수={}, 매도={}, MACD: 매수={}, 매도={}]", 
            market, bbBuySignal, bbSellSignal, rsiBuySignal, rsiSellSignal, macdBuySignal, macdSellSignal);
        return signal;
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
     * 조건: 현재가가 BB 하단 근처에 있을 때 (하단 대비 2% 이내)
     */
    private boolean calculateBollingerBandsBuySignal(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        Num currentPrice = closePrice.getValue(index);
        Num bbLower = getBollingerBandsLower(series, index);
        Num bbMiddle = getBollingerBandsMiddle(series, index);
        
        // 현재가가 BB 하단 근처에 있고, 중간선보다 낮을 때 매수 시그널
        double lowerThreshold = bbLower.doubleValue() * (1 + BB_LOWER_THRESHOLD);
        return currentPrice.doubleValue() <= lowerThreshold && currentPrice.isLessThan(bbMiddle);
    }
    
    /**
     * 볼린저밴드 매도 시그널 계산
     * 조건: 현재가가 BB 상단 근처에 있을 때 (상단 대비 98% 이상)
     */
    private boolean calculateBollingerBandsSellSignal(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        Num currentPrice = closePrice.getValue(index);
        Num bbUpper = getBollingerBandsUpper(series, index);
        Num bbMiddle = getBollingerBandsMiddle(series, index);
        
        // 현재가가 BB 상단 근처에 있고, 중간선보다 높을 때 매도 시그널
        double upperThreshold = bbUpper.doubleValue() * BB_UPPER_THRESHOLD;
        return currentPrice.doubleValue() >= upperThreshold && currentPrice.isGreaterThan(bbMiddle);
    }
    
    /**
     * RSI 매수 시그널 계산
     * 조건: RSI(9) < 30 (과매도 구간에서 반등 시그널)
     */
    private boolean calculateRSIBuySignal(BarSeries series, int index) {
        if (index <= 0) return false;
        
        RSIIndicator rsi = new RSIIndicator(new ClosePriceIndicator(series), RSI_PERIOD);
        Num currentRsi = rsi.getValue(index);
        Num previousRsi = rsi.getValue(index - 1);
        
        // RSI가 과매도 구간이고, 이전보다 상승하고 있을 때 매수 시그널
        return currentRsi.isLessThan(series.numOf(RSI_OVERSOLD)) && 
               currentRsi.isGreaterThan(previousRsi);
    }
    
    /**
     * RSI 매도 시그널 계산
     * 조건: RSI > 70 (과매수 구간에서 하락 시그널)
     */
    private boolean calculateRSISellSignal(BarSeries series, int index) {
        if (index <= 0) return false;
        
        RSIIndicator rsi = new RSIIndicator(new ClosePriceIndicator(series), RSI_PERIOD);
        Num currentRsi = rsi.getValue(index);
        Num previousRsi = rsi.getValue(index - 1);
        
        // RSI가 과매수 구간이고, 이전보다 하락하고 있을 때 매도 시그널
        return currentRsi.isGreaterThan(series.numOf(RSI_OVERBOUGHT)) && 
               currentRsi.isLessThan(previousRsi);
    }
    
    /**
     * MACD 매수 시그널 계산
     * 조건: MACD가 시그널선을 상향 돌파하거나, 히스토그램이 증가하면서 MACD가 양수일 때
     */
    private boolean calculateMACDBuySignal(BarSeries series, int index) {
        if (index <= 0) return false;
        
        Num currentMacd = getMACDValue(series, index);
        Num currentSignal = getMACDSignalValue(series, index);
        Num currentHistogram = getMACDHistogram(series, index);
        
        Num previousMacd = getMACDValue(series, index - 1);
        Num previousSignal = getMACDSignalValue(series, index - 1);
        Num previousHistogram = getMACDHistogram(series, index - 1);
        
        // 조건 1: MACD가 시그널선을 상향 돌파
        boolean macdCrossUp = previousMacd.isLessThan(previousSignal) && 
                             currentMacd.isGreaterThan(currentSignal);
        
        // 조건 2: 히스토그램이 증가하면서 MACD가 양수
        boolean histogramIncrease = currentHistogram.isGreaterThan(previousHistogram) && 
                                   currentMacd.isGreaterThan(series.numOf(0));
        
        return macdCrossUp || histogramIncrease;
    }
    
    /**
     * MACD 매도 시그널 계산
     * 조건: MACD가 시그널선을 하향 돌파하거나, 히스토그램이 감소하면서 MACD가 음수일 때
     */
    private boolean calculateMACDSellSignal(BarSeries series, int index) {
        if (index <= 0) return false;
        
        Num currentMacd = getMACDValue(series, index);
        Num currentSignal = getMACDSignalValue(series, index);
        Num currentHistogram = getMACDHistogram(series, index);
        
        Num previousMacd = getMACDValue(series, index - 1);
        Num previousSignal = getMACDSignalValue(series, index - 1);
        Num previousHistogram = getMACDHistogram(series, index - 1);
        
        // 조건 1: MACD가 시그널선을 하향 돌파
        boolean macdCrossDown = previousMacd.isGreaterThan(previousSignal) && 
                               currentMacd.isLessThan(currentSignal);
        
        // 조건 2: 히스토그램이 감소하면서 MACD가 음수
        boolean histogramDecrease = currentHistogram.isLessThan(previousHistogram) && 
                                   currentMacd.isLessThan(series.numOf(0));
        
        return macdCrossDown || histogramDecrease;
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