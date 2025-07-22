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
    
    public BarSeries createBarSeries(String market) {
        // 최근 200개의 1분봉 데이터 조회
        List<MinuteCandleResponse> candles = upbitQuotationClient.getMinuteCandles(1, market, null, 200);
        
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
        
        // 이동평균선
        SMAIndicator sma5 = new SMAIndicator(closePrice, 5);
        SMAIndicator sma20 = new SMAIndicator(closePrice, 20);
        
        // MACD
        MACDIndicator macd = new MACDIndicator(closePrice);
        SMAIndicator signal = new SMAIndicator(macd, 9);
        
        // RSI
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        
        // Bollinger Bands
        SMAIndicator sma20BB = new SMAIndicator(closePrice, 20);
        StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, 20);
        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(sma20BB);
        BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, sd);
        BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, sd);
        
        int lastIndex = series.getEndIndex();
        
        // MA 크로스 시그널
        boolean goldenCross = sma5.getValue(lastIndex - 1).isLessThan(sma20.getValue(lastIndex - 1)) &&
                            sma5.getValue(lastIndex).isGreaterThan(sma20.getValue(lastIndex));
                            
        boolean deadCross = sma5.getValue(lastIndex - 1).isGreaterThan(sma20.getValue(lastIndex - 1)) &&
                           sma5.getValue(lastIndex).isLessThan(sma20.getValue(lastIndex));
        
        // MACD 시그널
        boolean macdBuySignal = macd.getValue(lastIndex - 1).isLessThan(signal.getValue(lastIndex - 1)) &&
                               macd.getValue(lastIndex).isGreaterThan(signal.getValue(lastIndex));
                               
        boolean macdSellSignal = macd.getValue(lastIndex - 1).isGreaterThan(signal.getValue(lastIndex - 1)) &&
                                macd.getValue(lastIndex).isLessThan(signal.getValue(lastIndex));
        
        // RSI 시그널
        boolean rsiOversold = rsi.getValue(lastIndex).isLessThan(series.numOf(30));
        boolean rsiOverbought = rsi.getValue(lastIndex).isGreaterThan(series.numOf(70));
        
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