package com.everbit.everbit.upbit.service;

import com.everbit.everbit.upbit.dto.trading.TradingSignal;

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
    
    /**
     * 모든 마켓에 대한 기본 기술적 지표 시그널을 계산합니다.
     */
    public TradingSignal calculateBasicSignals(String market) {
        BarSeries series = candleDataService.createBarSeries(market);
        return calculateSignalsFromSeries(series, market);
    }
    
    /**
     * 시리즈로부터 기본 기술적 지표 시그널을 계산합니다.
     */
    private TradingSignal calculateSignalsFromSeries(BarSeries series, String market) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        int lastIndex = series.getEndIndex();
        
        // 개별 지표 시그널 계산
        boolean bbBuySignal = isBollingerBandsBuySignal(series, lastIndex);
        boolean bbSellSignal = isBollingerBandsSellSignal(series, lastIndex);
        boolean rsiBuySignal = isRSIBuySignal(series, lastIndex);
        boolean rsiSellSignal = isRSISellSignal(series, lastIndex);
        boolean macdBuySignal = isMACDBuySignal(series, lastIndex);
        boolean macdSellSignal = isMACDSellSignal(series, lastIndex);
        
        // 매수/매도 시그널 충돌 해결
        resolveSignalConflicts(series, lastIndex, bbBuySignal, bbSellSignal, rsiBuySignal, rsiSellSignal, macdBuySignal, macdSellSignal);
        
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
        
        return signal;
    }
    
    /**
     * 매수/매도 시그널 충돌을 해결합니다.
     */
    private void resolveSignalConflicts(BarSeries series, int index, 
                                      boolean bbBuySignal, boolean bbSellSignal,
                                      boolean rsiBuySignal, boolean rsiSellSignal,
                                      boolean macdBuySignal, boolean macdSellSignal) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        Num currentPrice = closePrice.getValue(index);
        Num bbMiddle = getBollingerBandsMiddle(series, index);
        Num rsiValue = getRSIValue(series, index);
        Num macdValue = getMACDValue(series, index);
        
        // 볼린저밴드 충돌 해결: 현재가 위치에 따라 결정
        if (bbBuySignal && bbSellSignal) {
            bbSellSignal = currentPrice.isGreaterThan(bbMiddle);
            bbBuySignal = !bbSellSignal;
        }
        
        // RSI 충돌 해결: 중간값(50) 기준으로 결정
        if (rsiBuySignal && rsiSellSignal) {
            rsiSellSignal = rsiValue.isGreaterThan(series.numOf(50));
            rsiBuySignal = !rsiSellSignal;
        }
        
        // MACD 충돌 해결: MACD 값의 부호에 따라 결정
        if (macdBuySignal && macdSellSignal) {
            macdSellSignal = macdValue.isLessThan(series.numOf(0));
            macdBuySignal = !macdSellSignal;
        }
    }
    
    // ==================== 개별 지표 시그널 계산 메서드들 ====================
    
    /**
     * 볼린저밴드 매수 시그널 계산
     * 조건: 현재가가 BB 하단 근처에 있을 때 (하단 대비 2% 이내)
     */
    private boolean isBollingerBandsBuySignal(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        Num currentPrice = closePrice.getValue(index);
        Num bbLower = getBollingerBandsLower(series, index);
        Num bbMiddle = getBollingerBandsMiddle(series, index);
        
        // 현재가가 BB 하단 근처에 있고, 중간선보다 낮을 때 매수 시그널
        double lowerThreshold = bbLower.doubleValue() * (1 + BB_LOWER_THRESHOLD);
        boolean isNearLower = currentPrice.doubleValue() <= lowerThreshold;
        boolean isBelowMiddle = currentPrice.isLessThan(bbMiddle);
        boolean buySignal = isNearLower && isBelowMiddle;
        
        log.debug("볼린저밴드 매수 시그널 계산 - 현재가: {}, BB하단: {}, BB중간: {}, 하단임계값: {}, 하단근처: {}, 중간선아래: {}, 매수시그널: {}", 
            currentPrice.doubleValue(), bbLower.doubleValue(), bbMiddle.doubleValue(), 
            lowerThreshold, isNearLower, isBelowMiddle, buySignal);
        
        return buySignal;
    }
    
    /**
     * 볼린저밴드 매도 시그널 계산
     * 조건: 현재가가 BB 상단 근처에 있을 때 (상단 대비 98% 이상)
     */
    private boolean isBollingerBandsSellSignal(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        Num currentPrice = closePrice.getValue(index);
        Num bbUpper = getBollingerBandsUpper(series, index);
        Num bbMiddle = getBollingerBandsMiddle(series, index);
        
        // 현재가가 BB 상단 근처에 있고, 중간선보다 높을 때 매도 시그널
        double upperThreshold = bbUpper.doubleValue() * BB_UPPER_THRESHOLD;
        boolean isNearUpper = currentPrice.doubleValue() >= upperThreshold;
        boolean isAboveMiddle = currentPrice.isGreaterThan(bbMiddle);
        boolean sellSignal = isNearUpper && isAboveMiddle;
        
        log.debug("볼린저밴드 매도 시그널 계산 - 현재가: {}, BB상단: {}, BB중간: {}, 상단임계값: {}, 상단근처: {}, 중간선위: {}, 매도시그널: {}", 
            currentPrice.doubleValue(), bbUpper.doubleValue(), bbMiddle.doubleValue(), 
            upperThreshold, isNearUpper, isAboveMiddle, sellSignal);
        
        return sellSignal;
    }
    
    /**
     * RSI 매수 시그널 계산
     * 조건: RSI(9) < 30 (과매도 구간에서 매수 시그널)
     */
    private boolean isRSIBuySignal(BarSeries series, int index) {
        if (index <= 0) return false;
        
        RSIIndicator rsi = new RSIIndicator(new ClosePriceIndicator(series), RSI_PERIOD);
        Num currentRsi = rsi.getValue(index);
        
        // RSI가 과매도 구간에 있을 때 매수 시그널 (상승 여부와 관계없이)
        boolean buySignal = currentRsi.isLessThan(series.numOf(RSI_OVERSOLD));
        
        log.debug("RSI 매수 시그널 계산 - 현재RSI: {}, 과매도기준: {}, 매수시그널: {}", 
            currentRsi.doubleValue(), RSI_OVERSOLD, buySignal);
        
        return buySignal;
    }
    
    /**
     * RSI 매도 시그널 계산
     * 조건: RSI > 70 (과매수 구간에서 매도 시그널)
     */
    private boolean isRSISellSignal(BarSeries series, int index) {
        if (index <= 0) return false;
        
        RSIIndicator rsi = new RSIIndicator(new ClosePriceIndicator(series), RSI_PERIOD);
        Num currentRsi = rsi.getValue(index);
        
        // RSI가 과매수 구간에 있을 때 매도 시그널
        boolean sellSignal = currentRsi.isGreaterThan(series.numOf(RSI_OVERBOUGHT));
        
        log.debug("RSI 매도 시그널 계산 - 현재RSI: {}, 과매수기준: {}, 매도시그널: {}", 
            currentRsi.doubleValue(), RSI_OVERBOUGHT, sellSignal);
        
        return sellSignal;
    }
    
    /**
     * MACD 매수 시그널 계산
     * 조건: MACD가 시그널선을 상향 돌파하거나, 히스토그램이 증가하면서 MACD가 양수일 때
     */
    private boolean isMACDBuySignal(BarSeries series, int index) {
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
        
        boolean buySignal = macdCrossUp || histogramIncrease;
        
        log.debug("MACD 매수 시그널 계산 - 현재MACD: {}, 현재시그널: {}, 현재히스토그램: {}, 이전MACD: {}, 이전시그널: {}, 이전히스토그램: {}, 상향돌파: {}, 히스토그램증가: {}, 매수시그널: {}", 
            currentMacd.doubleValue(), currentSignal.doubleValue(), currentHistogram.doubleValue(),
            previousMacd.doubleValue(), previousSignal.doubleValue(), previousHistogram.doubleValue(),
            macdCrossUp, histogramIncrease, buySignal);
        
        return buySignal;
    }
    
    /**
     * MACD 매도 시그널 계산
     * 조건: MACD가 시그널선을 하향 돌파하거나, 히스토그램이 감소하면서 MACD가 음수일 때
     */
    private boolean isMACDSellSignal(BarSeries series, int index) {
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
        
        boolean sellSignal = macdCrossDown || histogramDecrease;
        
        log.debug("MACD 매도 시그널 계산 - 현재MACD: {}, 현재시그널: {}, 현재히스토그램: {}, 이전MACD: {}, 이전시그널: {}, 이전히스토그램: {}, 하향돌파: {}, 히스토그램감소: {}, 매도시그널: {}", 
            currentMacd.doubleValue(), currentSignal.doubleValue(), currentHistogram.doubleValue(),
            previousMacd.doubleValue(), previousSignal.doubleValue(), previousHistogram.doubleValue(),
            macdCrossDown, histogramDecrease, sellSignal);
        
        return sellSignal;
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