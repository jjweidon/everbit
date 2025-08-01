package com.everbit.everbit.upbit.service;

import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.trade.entity.enums.Strategy;
import com.everbit.everbit.upbit.dto.quotation.MinuteCandleResponse;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 캔들 데이터 처리 및 BarSeries 생성을 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
public class CandleDataService {
    private final UpbitQuotationClient upbitQuotationClient;
    private static final ZoneId UTC = ZoneId.of("UTC");
    
    /**
     * 사용자의 전략에 맞는 최적화된 캔들 설정으로 BarSeries를 생성합니다.
     */
    public BarSeries createBarSeries(String market, User user) {
        // 전략에서 직접 캔들 설정 가져오기
        Strategy strategy = user.getBotSetting().getStrategy();
        
        // 최근 캔들 데이터 조회
        List<MinuteCandleResponse> candles = upbitQuotationClient.getMinuteCandles(
            strategy.getCandleInterval(), 
            market, 
            null, 
            strategy.getCandleCount()
        );
        
        return buildBarSeries(market, candles);
    }
    
    /**
     * 지정된 전략으로 BarSeries를 생성합니다.
     */
    public BarSeries createBarSeries(String market, Strategy strategy) {
        // 최근 캔들 데이터 조회
        List<MinuteCandleResponse> candles = upbitQuotationClient.getMinuteCandles(
            strategy.getCandleInterval(), 
            market, 
            null, 
            strategy.getCandleCount()
        );
        
        return buildBarSeries(market, candles);
    }
    
    /**
     * 캔들 데이터로부터 BarSeries를 빌드합니다.
     */
    private BarSeries buildBarSeries(String market, List<MinuteCandleResponse> candles) {
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
    
    /**
     * BarSeries의 현재 가격을 반환합니다.
     */
    public double getCurrentPrice(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        int lastIndex = series.getEndIndex();
        return closePrice.getValue(lastIndex).doubleValue();
    }
    
    /**
     * BarSeries의 데이터 유효성을 검증합니다.
     */
    public boolean isValidBarSeries(BarSeries series) {
        return series != null && series.getBarCount() > 0;
    }
    
    /**
     * BarSeries의 통계 정보를 반환합니다.
     */
    public BarSeriesStats getBarSeriesStats(BarSeries series) {
        if (!isValidBarSeries(series)) {
            return null;
        }
        
        int barCount = series.getBarCount();
        ZonedDateTime startTime = series.getFirstBar().getBeginTime();
        ZonedDateTime endTime = series.getLastBar().getEndTime();
        
        return new BarSeriesStats(barCount, startTime, endTime);
    }
    
    /**
     * BarSeries 통계 정보를 담는 클래스
     */
    public static class BarSeriesStats {
        private final int barCount;
        private final ZonedDateTime startTime;
        private final ZonedDateTime endTime;
        
        public BarSeriesStats(int barCount, ZonedDateTime startTime, ZonedDateTime endTime) {
            this.barCount = barCount;
            this.startTime = startTime;
            this.endTime = endTime;
        }
        
        public int getBarCount() {
            return barCount;
        }
        
        public ZonedDateTime getStartTime() {
            return startTime;
        }
        
        public ZonedDateTime getEndTime() {
            return endTime;
        }
        
        @Override
        public String toString() {
            return String.format("BarSeriesStats{barCount=%d, startTime=%s, endTime=%s}", 
                               barCount, startTime, endTime);
        }
    }
} 