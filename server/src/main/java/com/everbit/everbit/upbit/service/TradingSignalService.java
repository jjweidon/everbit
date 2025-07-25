package com.everbit.everbit.upbit.service;

import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

import com.everbit.everbit.upbit.dto.quotation.MinuteCandleResponse;
import com.everbit.everbit.upbit.dto.trading.TradingSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradingSignalService {
    private final UpbitQuotationClient upbitQuotationClient;
    private static final ZoneId UTC = ZoneId.of("UTC");
    
    // 기술적 지표 계산을 위한 상수 정의
    private static final int CANDLE_INTERVAL = 5; // 5분봉
    private static final int CANDLE_COUNT = 200; // 데이터 포인트 수 증가
    private static final int SHORT_SMA = 5;  // 단기 이동평균선 기간
    private static final int LONG_SMA = 20;  // 장기 이동평균선 기간
    private static final int RSI_PERIOD = 9; // RSI 기간
    private static final int BB_PERIOD = 10; // 볼린저 밴드 기간
    private static final int MACD_SHORT = 6; // MACD 단기
    private static final int MACD_LONG = 13; // MACD 장기
    private static final int MACD_SIGNAL = 5; // MACD 시그널
    private static final double CROSS_THRESHOLD = 0.005; // 크로스 시그널 임계값 (0.5%)
    
    // 통계적 평균회귀 강화 상수
    private static final int Z_SCORE_PERIOD = 20; // Z-Score 계산 기간
    private static final double Z_SCORE_BUY_THRESHOLD = -1.5; // Z-Score 매수 임계값
    private static final double Z_SCORE_SELL_THRESHOLD = 1.5; // Z-Score 매도 임계값
    private static final int MOVING_AVERAGE_PERIOD = 20; // 이동평균 기간
    private static final double VOLATILITY_THRESHOLD = 0.015; // 변동성 임계값 (1.5%)
    
    public BarSeries createBarSeries(String market) {
        // 최근 50개의 5분봉 데이터 조회
        List<MinuteCandleResponse> candles = upbitQuotationClient.getMinuteCandles(CANDLE_INTERVAL, market, null, CANDLE_COUNT);
        
        // BarSeries 생성
        BarSeries series = new BaseBarSeriesBuilder().withName(market).build();
        
        // 캔들 데이터를 BarSeries에 추가 (시간 순서대로)
        for (int i = candles.size() - 1; i >= 0; i--) {
            MinuteCandleResponse candle = candles.get(i);
            ZonedDateTime zonedDateTime = candle.candleDateTimeUtc().atZone(UTC);
            
            series.addBar(
                zonedDateTime,
                candle.openingPrice(),
                candle.highPrice(),
                candle.lowPrice(),
                candle.tradePrice(),
                candle.candleAccTradeVolume()
            );
        }
        
        return series;
    }
    
    public TradingSignal calculateSignals(String market) {
        BarSeries series = createBarSeries(market);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        
        // 이동평균선 (EMA 5일, 20일)
        EMAIndicator emaShort = new EMAIndicator(closePrice, SHORT_SMA);
        EMAIndicator emaLong = new EMAIndicator(closePrice, LONG_SMA);
        
        // MACD (6, 13, 5)
        MACDIndicator macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
        EMAIndicator signal = new EMAIndicator(macd, MACD_SIGNAL);
        
        // RSI (9일)
        RSIIndicator rsi = new RSIIndicator(closePrice, RSI_PERIOD);
        
        // Bollinger Bands (10일)
        EMAIndicator emaBB = new EMAIndicator(closePrice, BB_PERIOD);
        StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, BB_PERIOD);
        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(emaBB);
        BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, sd);
        BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, sd);
        
        int lastIndex = series.getEndIndex();
        
        // EMA 크로스 시그널 (임계값 적용)
        double emaShortCurrent = emaShort.getValue(lastIndex).doubleValue();
        double emaLongCurrent = emaLong.getValue(lastIndex).doubleValue();
        double emaShortPrevious = emaShort.getValue(lastIndex - 1).doubleValue();
        double emaLongPrevious = emaLong.getValue(lastIndex - 1).doubleValue();
        
        // 골든크로스: 단기선이 장기선을 상향 돌파하고, 차이가 임계값 이상일 때
        boolean goldenCross = emaShortPrevious < emaLongPrevious &&
                            emaShortCurrent > emaLongCurrent &&
                            (emaShortCurrent - emaLongCurrent) / emaLongCurrent >= CROSS_THRESHOLD;
                            
        // 데드크로스: 단기선이 장기선을 하향 돌파하고, 차이가 임계값 이상일 때
        boolean deadCross = emaShortPrevious > emaLongPrevious &&
                           emaShortCurrent < emaLongCurrent &&
                           (emaLongCurrent - emaShortCurrent) / emaLongCurrent >= CROSS_THRESHOLD;
        
        // MACD 시그널
        boolean macdBuySignal = macd.getValue(lastIndex - 1).isLessThan(signal.getValue(lastIndex - 1)) &&
                               macd.getValue(lastIndex).isGreaterThan(signal.getValue(lastIndex));
                               
        boolean macdSellSignal = macd.getValue(lastIndex - 1).isGreaterThan(signal.getValue(lastIndex - 1)) &&
                                macd.getValue(lastIndex).isLessThan(signal.getValue(lastIndex));
        
        // RSI 시그널 (과매수/과매도 기준값 조정)
        boolean rsiOversold = rsi.getValue(lastIndex).isLessThan(series.numOf(35)); // 35로 상향 조정
        boolean rsiOverbought = rsi.getValue(lastIndex).isGreaterThan(series.numOf(65)); // 65로 하향 조정
        
        // Bollinger Bands 시그널
        boolean bbOverSold = closePrice.getValue(lastIndex).isLessThan(bbl.getValue(lastIndex));
        boolean bbOverBought = closePrice.getValue(lastIndex).isGreaterThan(bbu.getValue(lastIndex));
        
        // 개선된 평균 회귀 시그널 (통계적 접근)
        boolean meanReversionBuySignal = calculateEnhancedMeanReversionBuySignal(series, closePrice, lastIndex);
        boolean meanReversionSellSignal = calculateEnhancedMeanReversionSellSignal(series, closePrice, lastIndex);
        
        return new TradingSignal(
            market,
            series.getLastBar().getEndTime(),
            closePrice.getValue(lastIndex),
            goldenCross,
            deadCross,
            macdBuySignal,
            macdSellSignal,
            rsiOversold,
            rsiOverbought,
            bbOverSold,
            bbOverBought,
            meanReversionBuySignal,
            meanReversionSellSignal
        );
    }
    
    // 개선된 평균 회귀 매수 시그널 계산 (통계적 접근)
    private boolean calculateEnhancedMeanReversionBuySignal(BarSeries series, ClosePriceIndicator closePrice, int lastIndex) {
        if (lastIndex < Z_SCORE_PERIOD) {
            return false;
        }
        
        // 1. Z-Score 계산
        double zScore = calculateZScore(series, closePrice, lastIndex);
        
        // 2. 이동평균 비교
        double currentPrice = closePrice.getValue(lastIndex).doubleValue();
        double movingAverage = calculateMovingAverage(closePrice, lastIndex, MOVING_AVERAGE_PERIOD);
        
        // 3. 변동성 체크
        double volatility = calculateVolatility(closePrice, lastIndex, MOVING_AVERAGE_PERIOD);
        
        // 4. 볼린저 밴드 하단 체크
        boolean bbOversold = closePrice.getValue(lastIndex).isLessThan(
            new BollingerBandsLowerIndicator(
                new BollingerBandsMiddleIndicator(new EMAIndicator(closePrice, BB_PERIOD)),
                new StandardDeviationIndicator(closePrice, BB_PERIOD)
            ).getValue(lastIndex)
        );
        
        // 매수 조건: Z-Score 과매도 + 이동평균 아래 + 적절한 변동성 + 볼린저 밴드 하단
        return zScore <= Z_SCORE_BUY_THRESHOLD && 
               currentPrice < movingAverage * 0.995 && // 이동평균보다 0.5% 이상 낮음
               volatility >= VOLATILITY_THRESHOLD && 
               bbOversold;
    }
    
    // 개선된 평균 회귀 매도 시그널 계산 (통계적 접근)
    private boolean calculateEnhancedMeanReversionSellSignal(BarSeries series, ClosePriceIndicator closePrice, int lastIndex) {
        if (lastIndex < Z_SCORE_PERIOD) {
            return false;
        }
        
        // 1. Z-Score 계산
        double zScore = calculateZScore(series, closePrice, lastIndex);
        
        // 2. 이동평균 비교
        double currentPrice = closePrice.getValue(lastIndex).doubleValue();
        double movingAverage = calculateMovingAverage(closePrice, lastIndex, MOVING_AVERAGE_PERIOD);
        
        // 3. 변동성 체크
        double volatility = calculateVolatility(closePrice, lastIndex, MOVING_AVERAGE_PERIOD);
        
        // 4. 볼린저 밴드 상단 체크
        boolean bbOverbought = closePrice.getValue(lastIndex).isGreaterThan(
            new BollingerBandsUpperIndicator(
                new BollingerBandsMiddleIndicator(new EMAIndicator(closePrice, BB_PERIOD)),
                new StandardDeviationIndicator(closePrice, BB_PERIOD)
            ).getValue(lastIndex)
        );
        
        // 매도 조건: Z-Score 과매수 + 이동평균 위 + 적절한 변동성 + 볼린저 밴드 상단
        return zScore >= Z_SCORE_SELL_THRESHOLD && 
               currentPrice > movingAverage * 1.005 && // 이동평균보다 0.5% 이상 높음
               volatility >= VOLATILITY_THRESHOLD && 
               bbOverbought;
    }
    
    // Z-Score 계산
    private double calculateZScore(BarSeries series, ClosePriceIndicator closePrice, int lastIndex) {
        if (lastIndex < Z_SCORE_PERIOD) {
            return 0.0;
        }
        
        // 최근 Z_SCORE_PERIOD 기간의 가격 데이터 수집
        double[] prices = new double[Z_SCORE_PERIOD];
        for (int i = 0; i < Z_SCORE_PERIOD; i++) {
            prices[i] = closePrice.getValue(lastIndex - i).doubleValue();
        }
        
        // 평균 계산
        double mean = 0.0;
        for (double price : prices) {
            mean += price;
        }
        mean /= Z_SCORE_PERIOD;
        
        // 표준편차 계산
        double variance = 0.0;
        for (double price : prices) {
            variance += Math.pow(price - mean, 2);
        }
        double stdDev = Math.sqrt(variance / Z_SCORE_PERIOD);
        
        // Z-Score 계산
        double currentPrice = closePrice.getValue(lastIndex).doubleValue();
        return stdDev > 0 ? (currentPrice - mean) / stdDev : 0.0;
    }
    
    // 이동평균 계산
    private double calculateMovingAverage(ClosePriceIndicator closePrice, int lastIndex, int period) {
        if (lastIndex < period) {
            return closePrice.getValue(lastIndex).doubleValue();
        }
        
        double sum = 0.0;
        for (int i = 0; i < period; i++) {
            sum += closePrice.getValue(lastIndex - i).doubleValue();
        }
        return sum / period;
    }
    
    // 변동성 계산 (표준편차 기반)
    private double calculateVolatility(ClosePriceIndicator closePrice, int lastIndex, int period) {
        if (lastIndex < period) {
            return 0.0;
        }
        
        double mean = calculateMovingAverage(closePrice, lastIndex, period);
        double variance = 0.0;
        
        for (int i = 0; i < period; i++) {
            double price = closePrice.getValue(lastIndex - i).doubleValue();
            variance += Math.pow(price - mean, 2);
        }
        
        double stdDev = Math.sqrt(variance / period);
        return stdDev / mean; // 변동성 (표준편차/평균)
    }
} 