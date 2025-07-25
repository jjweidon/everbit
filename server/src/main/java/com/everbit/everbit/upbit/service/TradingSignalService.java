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
    
    // 평균 회귀 전략을 위한 상수
    private static final int MEAN_REVERSION_LOOKBACK = 20; // 20캔들 전 비교
    private static final int MEAN_REVERSION_SHORT_LOOKBACK = 1; // 5분캔들 전후 비교 (1캔들)
    private static final double MEAN_REVERSION_THRESHOLD = 0.02; // 2% 이상 차이 시 시그널
    
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
        
        // 평균 회귀 시그널 (20캔들 전과 5분캔들 전후 비교)
        boolean meanReversionBuySignal = calculateMeanReversionBuySignal(series, closePrice, lastIndex);
        boolean meanReversionSellSignal = calculateMeanReversionSellSignal(series, closePrice, lastIndex);
        
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
    
    // 평균 회귀 매수 시그널 계산 (20캔들 전과 5분캔들 전후 비교)
    private boolean calculateMeanReversionBuySignal(BarSeries series, ClosePriceIndicator closePrice, int lastIndex) {
        if (lastIndex < MEAN_REVERSION_LOOKBACK + MEAN_REVERSION_SHORT_LOOKBACK) {
            return false; // 충분한 데이터가 없으면 시그널 없음
        }
        
        // 현재 가격
        double currentPrice = closePrice.getValue(lastIndex).doubleValue();
        
        // 20캔들 전 가격
        double price20CandlesAgo = closePrice.getValue(lastIndex - MEAN_REVERSION_LOOKBACK).doubleValue();
        
        // 5분캔들 전 가격
        double price5MinAgo = closePrice.getValue(lastIndex - MEAN_REVERSION_SHORT_LOOKBACK).doubleValue();
        
        // 20캔들 전 대비 현재 가격이 2% 이상 하락했고, 5분캔들 전 대비도 하락 추세인 경우
        double dropFrom20Candles = (price20CandlesAgo - currentPrice) / price20CandlesAgo;
        double dropFrom5Min = (price5MinAgo - currentPrice) / price5MinAgo;
        
        return dropFrom20Candles >= MEAN_REVERSION_THRESHOLD && dropFrom5Min > 0;
    }
    
    // 평균 회귀 매도 시그널 계산 (20캔들 전과 5분캔들 전후 비교)
    private boolean calculateMeanReversionSellSignal(BarSeries series, ClosePriceIndicator closePrice, int lastIndex) {
        if (lastIndex < MEAN_REVERSION_LOOKBACK + MEAN_REVERSION_SHORT_LOOKBACK) {
            return false; // 충분한 데이터가 없으면 시그널 없음
        }
        
        // 현재 가격
        double currentPrice = closePrice.getValue(lastIndex).doubleValue();
        
        // 20캔들 전 가격
        double price20CandlesAgo = closePrice.getValue(lastIndex - MEAN_REVERSION_LOOKBACK).doubleValue();
        
        // 5분캔들 전 가격
        double price5MinAgo = closePrice.getValue(lastIndex - MEAN_REVERSION_SHORT_LOOKBACK).doubleValue();
        
        // 20캔들 전 대비 현재 가격이 2% 이상 상승했고, 5분캔들 전 대비도 상승 추세인 경우
        double riseFrom20Candles = (currentPrice - price20CandlesAgo) / price20CandlesAgo;
        double riseFrom5Min = (currentPrice - price5MinAgo) / price5MinAgo;
        
        return riseFrom20Candles >= MEAN_REVERSION_THRESHOLD && riseFrom5Min > 0;
    }
} 