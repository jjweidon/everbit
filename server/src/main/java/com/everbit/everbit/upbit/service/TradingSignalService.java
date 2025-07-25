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
import org.ta4j.core.indicators.SMAIndicator;

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
    
    // 캔들 설정
    private static final int CANDLE_MINUTES = 5;  // 5분봉 사용 (더 민감한 반응)
    private static final int CANDLE_COUNT = 200;  // EMA-200을 위한 최소 데이터 포인트
    
    // EMA 크로스 + 모멘텀 전략 파라미터
    private static final int EMA_SHORT = 9;   // 단기 EMA
    private static final int EMA_MID = 21;    // 중기 EMA
    private static final int EMA_50 = 50;     // 장기 EMA (골든크로스용)
    private static final int EMA_200 = 200;   // 초장기 EMA (골든크로스용)
    
    // MACD + RSI 전략 파라미터
    private static final int MACD_SHORT = 12;
    private static final int MACD_LONG = 26;
    private static final int MACD_SIGNAL = 9;
    private static final int RSI_PERIOD = 14;
    private static final int RSI_OVERSOLD = 30;
    private static final int RSI_OVERBOUGHT = 70;
    
    // 볼린저 밴드 + 모멘텀 전략 파라미터
    private static final int BB_PERIOD = 20;

    public BarSeries createBarSeries(String market) {
        // 캔들 데이터 조회
        List<MinuteCandleResponse> candles = upbitQuotationClient.getMinuteCandles(CANDLE_MINUTES, market, null, CANDLE_COUNT);
        
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
        
        // 1. EMA 크로스 + 모멘텀 전략
        EMAIndicator emaShort = new EMAIndicator(closePrice, EMA_SHORT);
        EMAIndicator emaMid = new EMAIndicator(closePrice, EMA_MID);
        EMAIndicator ema50 = new EMAIndicator(closePrice, EMA_50);
        EMAIndicator ema200 = new EMAIndicator(closePrice, EMA_200);
        
        // 2. MACD + RSI 전략
        MACDIndicator macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
        EMAIndicator signal = new EMAIndicator(macd, MACD_SIGNAL);
        RSIIndicator rsi = new RSIIndicator(closePrice, RSI_PERIOD);
        
        // 3. 볼린저밴드 전략
        SMAIndicator bbm = new SMAIndicator(closePrice, BB_PERIOD);
        StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, BB_PERIOD);
        BollingerBandsMiddleIndicator bbMiddle = new BollingerBandsMiddleIndicator(bbm);
        BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbMiddle, sd);
        BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbMiddle, sd);
        
        int lastIndex = series.getEndIndex();
        
        // EMA 크로스 시그널
        boolean goldenCross = emaShort.getValue(lastIndex).isGreaterThan(emaMid.getValue(lastIndex)) &&
                            emaShort.getValue(lastIndex - 1).isLessThan(emaMid.getValue(lastIndex - 1));
                            
        boolean deadCross = emaShort.getValue(lastIndex).isLessThan(emaMid.getValue(lastIndex)) &&
                           emaShort.getValue(lastIndex - 1).isGreaterThan(emaMid.getValue(lastIndex - 1));

        // 50/200 EMA 시그널
        boolean ema50Above200 = ema50.getValue(lastIndex).isGreaterThan(ema200.getValue(lastIndex));
        boolean ema50Below200 = ema50.getValue(lastIndex).isLessThan(ema200.getValue(lastIndex));
        boolean ema50Cross200Up = ema50.getValue(lastIndex).isGreaterThan(ema200.getValue(lastIndex)) &&
                                 ema50.getValue(lastIndex - 1).isLessThanOrEqual(ema200.getValue(lastIndex - 1));
        boolean ema50Cross200Down = ema50.getValue(lastIndex).isLessThan(ema200.getValue(lastIndex)) &&
                                   ema50.getValue(lastIndex - 1).isGreaterThanOrEqual(ema200.getValue(lastIndex - 1));
        
        // MACD 시그널
        boolean macdBuySignal = macd.getValue(lastIndex).isGreaterThan(signal.getValue(lastIndex)) &&
                               macd.getValue(lastIndex - 1).isLessThan(signal.getValue(lastIndex - 1));
                               
        boolean macdSellSignal = macd.getValue(lastIndex).isLessThan(signal.getValue(lastIndex)) &&
                                macd.getValue(lastIndex - 1).isGreaterThan(signal.getValue(lastIndex - 1));
        
        // RSI 시그널
        boolean rsiOversold = rsi.getValue(lastIndex).doubleValue() < RSI_OVERSOLD;
        boolean rsiOverbought = rsi.getValue(lastIndex).doubleValue() > RSI_OVERBOUGHT;
        
        // 볼린저밴드 시그널
        boolean bbOverSold = closePrice.getValue(lastIndex).isLessThan(bbl.getValue(lastIndex));
        boolean bbOverBought = closePrice.getValue(lastIndex).isGreaterThan(bbu.getValue(lastIndex));
        boolean priceAboveBBMiddle = closePrice.getValue(lastIndex).isGreaterThan(bbMiddle.getValue(lastIndex));
        boolean priceBelowBBMiddle = closePrice.getValue(lastIndex).isLessThan(bbMiddle.getValue(lastIndex));
        boolean bbMiddleTrendUp = bbMiddle.getValue(lastIndex).isGreaterThan(bbMiddle.getValue(lastIndex - 1));
        boolean bbMiddleTrendDown = bbMiddle.getValue(lastIndex).isLessThan(bbMiddle.getValue(lastIndex - 1));
        
        return new TradingSignal(
            market,
            series.getLastBar().getEndTime(),
            closePrice.getValue(lastIndex),
            goldenCross,
            deadCross,
            ema50Above200,
            ema50Below200,
            ema50Cross200Up,
            ema50Cross200Down,
            macdBuySignal,
            macdSellSignal,
            rsiOversold,
            rsiOverbought,
            bbOverSold,
            bbOverBought,
            priceAboveBBMiddle,
            priceBelowBBMiddle,
            bbMiddleTrendUp,
            bbMiddleTrendDown
        );
    }
} 