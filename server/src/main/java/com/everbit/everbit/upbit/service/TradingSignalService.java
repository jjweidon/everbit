package com.everbit.everbit.upbit.service;

import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.SMAIndicator;
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
    private static final int CANDLE_COUNT = 400; // 데이터 포인트 수 증가
    private static final int SHORT_SMA = 9;  // 단기 이동평균선 기간
    private static final int LONG_SMA = 21;  // 장기 이동평균선 기간
    private static final int RSI_PERIOD = 9; // RSI 기간
    private static final int BB_PERIOD = 10; // 볼린저 밴드 기간
    private static final int MACD_SHORT = 6; // MACD 단기
    private static final int MACD_LONG = 13; // MACD 장기
    private static final int MACD_SIGNAL = 5; // MACD 시그널
    
    public BarSeries createBarSeries(String market) {
        // 최근 50개의 5분봉 데이터 조회
        List<MinuteCandleResponse> candles = upbitQuotationClient.getMinuteCandles(3, market, null, CANDLE_COUNT);
        
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
        
        // 이동평균선 (3일, 10일)
        SMAIndicator smaShort = new SMAIndicator(closePrice, SHORT_SMA);
        SMAIndicator smaLong = new SMAIndicator(closePrice, LONG_SMA);
        
        // MACD (6, 13, 5)
        MACDIndicator macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
        SMAIndicator signal = new SMAIndicator(macd, MACD_SIGNAL);
        
        // RSI (9일)
        RSIIndicator rsi = new RSIIndicator(closePrice, RSI_PERIOD);
        
        // Bollinger Bands (10일)
        SMAIndicator smaBB = new SMAIndicator(closePrice, BB_PERIOD);
        StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, BB_PERIOD);
        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(smaBB);
        BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, sd);
        BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, sd);
        
        int lastIndex = series.getEndIndex();
        
        // MA 크로스 시그널
        boolean goldenCross = smaShort.getValue(lastIndex - 1).isLessThan(smaLong.getValue(lastIndex - 1)) &&
                            smaShort.getValue(lastIndex).isGreaterThan(smaLong.getValue(lastIndex));
                            
        boolean deadCross = smaShort.getValue(lastIndex - 1).isGreaterThan(smaLong.getValue(lastIndex - 1)) &&
                           smaShort.getValue(lastIndex).isLessThan(smaLong.getValue(lastIndex));
        
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
            bbOverBought
        );
    }
} 