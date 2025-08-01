package com.everbit.everbit.upbit.service;

import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.EMAIndicator;
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
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class TradingSignalService {
    private final CandleDataService candleDataService;
    
    // 기술적 지표 계산을 위한 상수 정의
    private static final int SHORT_EMA = 9;  // 단기 EMA 기간
    private static final int LONG_EMA = 21;  // 장기 EMA 기간
    private static final int RSI_PERIOD = 14; // RSI 기간
    private static final int BB_PERIOD = 20; // 볼린저 밴드 기간
    private static final int MACD_SHORT = 6; // MACD 단기
    private static final int MACD_LONG = 13; // MACD 장기
    private static final int MACD_SIGNAL = 5; // MACD 시그널
    private static final int RSI_BASE_OVERSOLD = 20; // RSI 과매도 기준
    private static final int RSI_BASE_OVERBOUGHT = 80; // RSI 과매수 기준
    
    public TradingSignal calculateSignals(String market, User user) {
        BarSeries series = candleDataService.createBarSeries(market, user);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        
        int lastIndex = series.getEndIndex();
        
        // 전략별 시그널 계산
        boolean bbMeanReversionBuySignal = calculateBollingerMeanReversionBuySignal(series, lastIndex);
        boolean bbMeanReversionSellSignal = calculateBollingerMeanReversionSellSignal(series, lastIndex);
        boolean bbMomentumBuySignal = calculateBollingerMomentumBuySignal(series, lastIndex);
        boolean bbMomentumSellSignal = calculateBollingerMomentumSellSignal(series, lastIndex);
        boolean emaMomentumBuySignal = calculateEmaMomentumBuySignal(series, lastIndex);
        boolean emaMomentumSellSignal = calculateEmaMomentumSellSignal(series, lastIndex);

        return new TradingSignal(
            market,
            series.getLastBar().getEndTime(),
            closePrice.getValue(lastIndex),
            bbMeanReversionBuySignal,
            bbMeanReversionSellSignal,
            bbMomentumBuySignal,
            bbMomentumSellSignal,
            emaMomentumBuySignal,
            emaMomentumSellSignal
        );
    }

    /**
     * 시그널 강도에 따른 주문 금액 계산
     * 시그널이 강할수록 maxOrderAmount에 가까워지고, 약할수록 baseOrderAmount에 가까워집니다.
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
            case BOLLINGER_MEAN_REVERSION:
                return calculateBollingerMeanReversionSignalStrength(signal);
            case BB_MOMENTUM:
                return calculateBollingerMomentumSignalStrength(signal);
            case EMA_MOMENTUM:
                return calculateEmaMomentumSignalStrength(signal);
            case ENSEMBLE:
                return calculateEnsembleSignalStrength(signal);
            case ENHANCED_ENSEMBLE:
                return calculateEnhancedEnsembleSignalStrength(signal);
            default:
                return 0.5; // 기본값
        }
    }
    
    /**
     * 볼린저 평균회귀 전략 시그널 강도 계산
     */
    private double calculateBollingerMeanReversionSignalStrength(TradingSignal signal) {
        if (signal.isBollingerMeanReversionBuySignal() || signal.isBollingerMeanReversionSellSignal()) {
            // RSI 기반 강도 계산 (RSI가 극단적일수록 강한 시그널)
            double rsiStrength = calculateRSIStrength(signal.market(), signal.timestamp());
            return rsiStrength;
        }
        return 0.0; // 시그널 없음
    }
    
    /**
     * 볼린저 모멘텀 전략 시그널 강도 계산
     */
    private double calculateBollingerMomentumSignalStrength(TradingSignal signal) {
        if (signal.isBbMomentumBuySignal() || signal.isBbMomentumSellSignal()) {
            // 볼린저 밴드 이탈 정도와 MACD 강도 기반 계산
            double bbStrength = calculateBollingerBandStrength(signal.market(), signal.timestamp());
            double macdStrength = calculateMACDStrength(signal.market(), signal.timestamp());
            return (bbStrength + macdStrength) / 2.0; // 두 지표의 평균
        }
        return 0.0; // 시그널 없음
    }
    
    /**
     * EMA 모멘텀 전략 시그널 강도 계산
     */
    private double calculateEmaMomentumSignalStrength(TradingSignal signal) {
        if (signal.isEmaMomentumBuySignal() || signal.isEmaMomentumSellSignal()) {
            // EMA 교차 각도와 MACD 강도 기반 계산
            double emaStrength = calculateEMAStrength(signal.market(), signal.timestamp());
            double macdStrength = calculateMACDStrength(signal.market(), signal.timestamp());
            return (emaStrength + macdStrength) / 2.0; // 두 지표의 평균
        }
        return 0.0; // 시그널 없음
    }
    
    /**
     * 앙상블 전략 시그널 강도 계산
     */
    private double calculateEnsembleSignalStrength(TradingSignal signal) {
        int buySignals = 0;
        int sellSignals = 0;
        
        if (signal.isEmaMomentumBuySignal()) buySignals++;
        if (signal.isBbMomentumBuySignal()) buySignals++;
        if (signal.isEmaMomentumSellSignal()) sellSignals++;
        if (signal.isBbMomentumSellSignal()) sellSignals++;
        
        int totalSignals = buySignals + sellSignals;
        if (totalSignals >= 2) {
            return 0.9; // 매우 강한 시그널 (여러 전략이 동시에 시그널)
        } else if (totalSignals == 1) {
            return 0.5; // 약한 시그널
        }
        return 0.0; // 시그널 없음
    }
    
    /**
     * 강화 앙상블 전략 시그널 강도 계산
     */
    private double calculateEnhancedEnsembleSignalStrength(TradingSignal signal) {
        int buySignals = 0;
        int sellSignals = 0;
        
        if (signal.isEmaMomentumBuySignal()) buySignals++;
        if (signal.isBbMomentumBuySignal()) buySignals++;
        if (signal.isBollingerMeanReversionBuySignal()) buySignals++;
        if (signal.isEmaMomentumSellSignal()) sellSignals++;
        if (signal.isBbMomentumSellSignal()) sellSignals++;
        if (signal.isBollingerMeanReversionSellSignal()) sellSignals++;
        
        int totalSignals = buySignals + sellSignals;
        if (totalSignals >= 3) {
            return 1.0; // 최대 강도 시그널
        } else if (totalSignals == 2) {
            return 0.8; // 강한 시그널
        } else if (totalSignals == 1) {
            return 0.4; // 약한 시그널
        }
        return 0.0; // 시그널 없음
    }
    
    /**
     * 볼린저 밴드 평균 회귀 매수 시그널 계산
     * 조건: 가격이 하단 밴드 터치 + RSI 과매도 + MACD 상승 신호
     */
    private boolean calculateBollingerMeanReversionBuySignal(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator smaBB = new SMAIndicator(closePrice, BB_PERIOD);
        StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, BB_PERIOD);
        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(smaBB);
        BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, sd);
        
        RSIIndicator rsi = new RSIIndicator(closePrice, RSI_PERIOD);
        MACDIndicator macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
        SMAIndicator signal = new SMAIndicator(macd, MACD_SIGNAL);
        
        // 현재 가격이 하단 밴드 터치 또는 하단 아래
        boolean priceAtLowerBand = closePrice.getValue(index).isLessThanOrEqual(bbl.getValue(index));
        
        // RSI 과매도 상태
        boolean rsiOversold = rsi.getValue(index).isLessThan(series.numOf(RSI_BASE_OVERSOLD));
        
        // MACD 상승 신호 (MACD가 시그널선을 상향 돌파)
        boolean macdRising = index > 0 && 
                           macd.getValue(index - 1).isLessThan(signal.getValue(index - 1)) &&
                           macd.getValue(index).isGreaterThan(signal.getValue(index));
        
        return priceAtLowerBand && rsiOversold && macdRising;
    }
    
    /**
     * 볼린저 밴드 평균 회귀 매도 시그널 계산
     * 조건: 가격이 중간 밴드 도달 + RSI 과매수 + MACD 하락 신호
     */
    private boolean calculateBollingerMeanReversionSellSignal(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator smaBB = new SMAIndicator(closePrice, BB_PERIOD);
        StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, BB_PERIOD);
        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(smaBB);
        BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, sd);
        
        RSIIndicator rsi = new RSIIndicator(closePrice, RSI_PERIOD);
        MACDIndicator macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
        SMAIndicator signal = new SMAIndicator(macd, MACD_SIGNAL);
        
        // 현재 가격이 중간 밴드 도달 또는 상단 밴드 터치
        boolean priceAtMiddleOrUpperBand = closePrice.getValue(index).isGreaterThanOrEqual(bbm.getValue(index)) ||
                                          closePrice.getValue(index).isGreaterThanOrEqual(bbu.getValue(index));
        
        // RSI 과매수 상태
        boolean rsiOverbought = rsi.getValue(index).isGreaterThan(series.numOf(RSI_BASE_OVERBOUGHT));
        
        // MACD 하락 신호 (MACD가 시그널선을 하향 돌파)
        boolean macdFalling = index > 0 && 
                            macd.getValue(index - 1).isGreaterThan(signal.getValue(index - 1)) &&
                            macd.getValue(index).isLessThan(signal.getValue(index));
        
        return priceAtMiddleOrUpperBand && (rsiOverbought || macdFalling);
    }
    
    /**
     * 볼린저 밴드 + 모멘텀 매수 시그널 계산
     * 조건: 가격이 볼린저 밴드 수렴 구간에서 이탈 + 모멘텀 지표 상승
     */
    private boolean calculateBollingerMomentumBuySignal(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator smaBB = new SMAIndicator(closePrice, BB_PERIOD);
        StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, BB_PERIOD);
        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(smaBB);
        BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, sd);
        
        MACDIndicator macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
        SMAIndicator signal = new SMAIndicator(macd, MACD_SIGNAL);
        
        // 볼린저 밴드 수렴 구간에서 이탈 (가격이 상단 밴드를 상향 돌파)
        boolean priceBreakout = closePrice.getValue(index).isGreaterThan(bbu.getValue(index));
        
        // 모멘텀 지표 상승 (MACD 상승 신호)
        boolean momentumRising = index > 0 && 
                               macd.getValue(index - 1).isLessThan(signal.getValue(index - 1)) &&
                               macd.getValue(index).isGreaterThan(signal.getValue(index));
        
        return priceBreakout && momentumRising;
    }
    
    /**
     * 볼린저 밴드 + 모멘텀 매도 시그널 계산
     * 조건: 가격이 볼린저 밴드 수렴 구간에서 이탈 + 모멘텀 지표 하락
     */
    private boolean calculateBollingerMomentumSellSignal(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator smaBB = new SMAIndicator(closePrice, BB_PERIOD);
        StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, BB_PERIOD);
        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(smaBB);
        BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, sd);
        
        MACDIndicator macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
        SMAIndicator signal = new SMAIndicator(macd, MACD_SIGNAL);
        
        // 볼린저 밴드 수렴 구간에서 이탈 (가격이 하단 밴드를 하향 돌파)
        boolean priceBreakdown = closePrice.getValue(index).isLessThan(bbl.getValue(index));
        
        // 모멘텀 지표 하락 (MACD 하락 신호)
        boolean momentumFalling = index > 0 && 
                                macd.getValue(index - 1).isGreaterThan(signal.getValue(index - 1)) &&
                                macd.getValue(index).isLessThan(signal.getValue(index));
        
        return priceBreakdown && momentumFalling;
    }
    
    /**
     * EMA 모멘텀 매수 시그널 계산
     * 조건: 단기/중기 EMA 교차 + MACD 상승 신호
     */
    private boolean calculateEmaMomentumBuySignal(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator emaShort = new EMAIndicator(closePrice, SHORT_EMA);
        EMAIndicator emaLong = new EMAIndicator(closePrice, LONG_EMA);
        MACDIndicator macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
        SMAIndicator signal = new SMAIndicator(macd, MACD_SIGNAL);
        
        // EMA 골든크로스
        boolean emaGoldenCross = index > 0 &&
                               emaShort.getValue(index - 1).isLessThan(emaLong.getValue(index - 1)) &&
                               emaShort.getValue(index).isGreaterThan(emaLong.getValue(index));
        
        // MACD 상승 신호
        boolean macdRising = index > 0 && 
                           macd.getValue(index - 1).isLessThan(signal.getValue(index - 1)) &&
                           macd.getValue(index).isGreaterThan(signal.getValue(index));
        
        return emaGoldenCross && macdRising;
    }
    
    /**
     * EMA 모멘텀 매도 시그널 계산
     * 조건: 단기/중기 EMA 교차 + MACD 하락 신호
     */
    private boolean calculateEmaMomentumSellSignal(BarSeries series, int index) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator emaShort = new EMAIndicator(closePrice, SHORT_EMA);
        EMAIndicator emaLong = new EMAIndicator(closePrice, LONG_EMA);
        MACDIndicator macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
        SMAIndicator signal = new SMAIndicator(macd, MACD_SIGNAL);
        
        // EMA 데드크로스
        boolean emaDeadCross = index > 0 &&
                             emaShort.getValue(index - 1).isGreaterThan(emaLong.getValue(index - 1)) &&
                             emaShort.getValue(index).isLessThan(emaLong.getValue(index));
        
        // MACD 하락 신호
        boolean macdFalling = index > 0 && 
                            macd.getValue(index - 1).isGreaterThan(signal.getValue(index - 1)) &&
                            macd.getValue(index).isLessThan(signal.getValue(index));
        
        return emaDeadCross && macdFalling;
    }
    
    /**
     * RSI 기반 시그널 강도 계산 (0.0 ~ 1.0)
     * RSI가 극단적일수록(0 또는 100에 가까울수록) 강한 시그널
     */
    private double calculateRSIStrength(String market, ZonedDateTime timestamp) {
        try {
            // 기본 전략으로 BarSeries 생성 (EMA_MOMENTUM 전략 사용)
            BarSeries series = candleDataService.createBarSeries(market, Strategy.EMA_MOMENTUM);
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            RSIIndicator rsi = new RSIIndicator(closePrice, RSI_PERIOD);
            
            int lastIndex = series.getEndIndex();
            if (lastIndex < 0) {
                return 0.5; // 기본값
            }
            
            double rsiValue = rsi.getValue(lastIndex).doubleValue();
            
            // RSI 값에 따른 강도 계산
            // RSI가 0 또는 100에 가까울수록 강한 시그널 (0.8 ~ 1.0)
            // RSI가 중간값(50)에 가까울수록 약한 시그널 (0.0 ~ 0.3)
            if (rsiValue <= 20 || rsiValue >= 80) {
                // 극단적 과매수/과매도 구간
                double extremeFactor = Math.min(rsiValue, 100 - rsiValue) / 20.0; // 0~20 구간을 0~1로 정규화
                return 0.8 + (0.2 * extremeFactor); // 0.8 ~ 1.0
            } else if (rsiValue <= 30 || rsiValue >= 70) {
                // 과매수/과매도 구간
                double moderateFactor = Math.min(rsiValue, 100 - rsiValue) / 30.0; // 0~30 구간을 0~1로 정규화
                return 0.5 + (0.3 * moderateFactor); // 0.5 ~ 0.8
            } else {
                // 중립 구간
                double neutralFactor = Math.abs(50 - rsiValue) / 20.0; // 0~20 구간을 0~1로 정규화
                return 0.2 + (0.3 * neutralFactor); // 0.2 ~ 0.5
            }
        } catch (Exception e) {
            // 예외 발생 시 기본값 반환
            return 0.5;
        }
    }
    
    /**
     * 볼린저 밴드 기반 시그널 강도 계산 (0.0 ~ 1.0)
     * 가격이 밴드 경계에서 멀수록 강한 시그널
     */
    private double calculateBollingerBandStrength(String market, ZonedDateTime timestamp) {
        try {
            BarSeries series = candleDataService.createBarSeries(market, Strategy.BB_MOMENTUM);
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            SMAIndicator smaBB = new SMAIndicator(closePrice, BB_PERIOD);
            StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, BB_PERIOD);
            BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(smaBB);
            BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, sd);
            BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, sd);
            
            int lastIndex = series.getEndIndex();
            if (lastIndex < 0) {
                return 0.5;
            }
            
            double currentPrice = closePrice.getValue(lastIndex).doubleValue();
            double upperBand = bbu.getValue(lastIndex).doubleValue();
            double lowerBand = bbl.getValue(lastIndex).doubleValue();
            double middleBand = bbm.getValue(lastIndex).doubleValue();
            
            // 가격이 밴드 경계에서 얼마나 멀리 있는지 계산
            double bandWidth = upperBand - lowerBand;
            if (bandWidth == 0) {
                return 0.5;
            }
            
            double pricePosition;
            if (currentPrice > upperBand) {
                // 상단 밴드 위에 있을 때
                pricePosition = (currentPrice - upperBand) / bandWidth;
                return Math.min(0.8 + (0.2 * pricePosition), 1.0);
            } else if (currentPrice < lowerBand) {
                // 하단 밴드 아래에 있을 때
                pricePosition = (lowerBand - currentPrice) / bandWidth;
                return Math.min(0.8 + (0.2 * pricePosition), 1.0);
            } else {
                // 밴드 내부에 있을 때
                pricePosition = Math.abs(currentPrice - middleBand) / (bandWidth / 2.0);
                return 0.3 + (0.5 * pricePosition);
            }
        } catch (Exception e) {
            return 0.5;
        }
    }
    
    /**
     * MACD 기반 시그널 강도 계산 (0.0 ~ 1.0)
     * MACD 히스토그램의 크기가 클수록 강한 시그널
     */
    private double calculateMACDStrength(String market, ZonedDateTime timestamp) {
        try {
            BarSeries series = candleDataService.createBarSeries(market, Strategy.EMA_MOMENTUM);
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            MACDIndicator macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
            SMAIndicator signal = new SMAIndicator(macd, MACD_SIGNAL);
            
            int lastIndex = series.getEndIndex();
            if (lastIndex < 0) {
                return 0.5;
            }
            
            double macdValue = macd.getValue(lastIndex).doubleValue();
            double signalValue = signal.getValue(lastIndex).doubleValue();
            double histogram = macdValue - signalValue;
            
            // MACD 히스토그램의 절대값이 클수록 강한 시그널
            double absHistogram = Math.abs(histogram);
            
            // 히스토그램 크기에 따른 강도 계산 (임계값은 경험적으로 설정)
            if (absHistogram > 100) {
                return 1.0; // 매우 강한 시그널
            } else if (absHistogram > 50) {
                return 0.8; // 강한 시그널
            } else if (absHistogram > 20) {
                return 0.6; // 중간 시그널
            } else if (absHistogram > 10) {
                return 0.4; // 약한 시그널
            } else {
                return 0.2; // 매우 약한 시그널
            }
        } catch (Exception e) {
            return 0.5;
        }
    }
    
    /**
     * EMA 기반 시그널 강도 계산 (0.0 ~ 1.0)
     * EMA 간격이 클수록 강한 시그널
     */
    private double calculateEMAStrength(String market, ZonedDateTime timestamp) {
        try {
            BarSeries series = candleDataService.createBarSeries(market, Strategy.EMA_MOMENTUM);
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            EMAIndicator emaShort = new EMAIndicator(closePrice, SHORT_EMA);
            EMAIndicator emaLong = new EMAIndicator(closePrice, LONG_EMA);
            
            int lastIndex = series.getEndIndex();
            if (lastIndex < 0) {
                return 0.5;
            }
            
            double emaShortValue = emaShort.getValue(lastIndex).doubleValue();
            double emaLongValue = emaLong.getValue(lastIndex).doubleValue();
            double emaDiff = Math.abs(emaShortValue - emaLongValue);
            double emaRatio = emaDiff / emaLongValue; // 백분율로 계산
            
            // EMA 차이 비율에 따른 강도 계산
            if (emaRatio > 0.05) { // 5% 이상 차이
                return 1.0; // 매우 강한 시그널
            } else if (emaRatio > 0.03) { // 3% 이상 차이
                return 0.8; // 강한 시그널
            } else if (emaRatio > 0.02) { // 2% 이상 차이
                return 0.6; // 중간 시그널
            } else if (emaRatio > 0.01) { // 1% 이상 차이
                return 0.4; // 약한 시그널
            } else {
                return 0.2; // 매우 약한 시그널
            }
        } catch (Exception e) {
            return 0.5;
        }
    }
} 