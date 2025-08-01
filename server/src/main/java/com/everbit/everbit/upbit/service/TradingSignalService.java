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

import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.upbit.dto.quotation.MinuteCandleResponse;
import com.everbit.everbit.upbit.dto.trading.TradingSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradingSignalService {
    private final UpbitQuotationClient upbitQuotationClient;
    private static final ZoneId UTC = ZoneId.of("UTC");
    
    // 기술적 지표 계산을 위한 상수 정의
    // private static final int CANDLE_INTERVAL = 3; // 3분봉
    // private static final int CANDLE_COUNT = 100; // 데이터 포인트 수
    private static final int SHORT_SMA = 5;  // 단기 이동평균선 기간
    private static final int LONG_SMA = 20;  // 장기 이동평균선 기간
    private static final int RSI_PERIOD = 14; // RSI 기간
    private static final int STOCH_RSI_PERIOD = 14; // Stoch RSI 기간
    private static final int STOCH_K_PERIOD = 3; // Stoch RSI %K 기간
    private static final int STOCH_D_PERIOD = 3; // Stoch RSI %D 기간
    private static final int BB_PERIOD = 20; // 볼린저 밴드 기간
    private static final int MACD_SHORT = 6; // MACD 단기
    private static final int MACD_LONG = 13; // MACD 장기
    private static final int MACD_SIGNAL = 5; // MACD 시그널
    private static final int RSI_BASE_OVERSOLD = 20; // RSI 과매도 기준
    private static final int RSI_BASE_OVERBOUGHT = 80; // RSI 과매수 기준
    private static final int STOCH_RSI_OVERSOLD = 8; // Stoch RSI 과매도 기준
    private static final int STOCH_RSI_OVERBOUGHT = 92; // Stoch RSI 과매수 기준
    
    public BarSeries createBarSeries(String market, User user) {
        int candleInterval = user.getBotSetting().getCandleInterval().getMinutes();
        int candleCount = user.getBotSetting().getCandleCount();
        // 최근 100개의 3분봉 데이터 조회 (200 → 100으로 개선)
        List<MinuteCandleResponse> candles = upbitQuotationClient.getMinuteCandles(candleInterval, market, null, candleCount);
        
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
     * StochRSI Raw 값 계산 (Fast %K)
     */
    private double calculateStochRSIRaw(BarSeries series, int index) {
        if (index < STOCH_RSI_PERIOD) {
            return 50.0; // 충분한 데이터가 없을 때 중간값 반환
        }
        
        // RSI 계산
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, STOCH_RSI_PERIOD);
        
        // Stoch RSI 계산을 위한 최고/최저 RSI 값 찾기
        double maxRsi = Double.MIN_VALUE;
        double minRsi = Double.MAX_VALUE;
        
        for (int i = index - STOCH_RSI_PERIOD + 1; i <= index; i++) {
            double rsiValue = rsi.getValue(i).doubleValue();
            maxRsi = Math.max(maxRsi, rsiValue);
            minRsi = Math.min(minRsi, rsiValue);
        }
        
        double currentRsi = rsi.getValue(index).doubleValue();
        
        // Stoch RSI 공식: (현재 RSI - 최저 RSI) / (최고 RSI - 최저 RSI) * 100
        if (maxRsi == minRsi) {
            return 50.0; // 분모가 0인 경우 중간값 반환
        }
        
        return ((currentRsi - minRsi) / (maxRsi - minRsi)) * 100.0;
    }
    
    /**
     * StochRSI %K 값 계산 (StochRSI Raw의 3기간 평균)
     */
    public double calculateStochRSI_K(BarSeries series, int index) {
        if (index < STOCH_RSI_PERIOD + STOCH_K_PERIOD - 1) {
            return 50.0; // 충분한 데이터가 없을 때 중간값 반환
        }
        
        // StochRSI Raw 값 3개 수집
        List<Double> rawValues = new ArrayList<>();
        
        for (int i = index - STOCH_K_PERIOD + 1; i <= index; i++) {
            double rawValue = calculateStochRSIRaw(series, i);
            rawValues.add(rawValue);
        }
        
        // %K = StochRSI Raw의 3기간 평균
        return rawValues.stream().mapToDouble(d -> d).average().orElse(50.0);
    }
    
    /**
     * StochRSI %D 값 계산 (%K의 3기간 평균)
     */
    public double calculateStochRSI_D(BarSeries series, int index) {
        if (index < STOCH_RSI_PERIOD + STOCH_K_PERIOD + STOCH_D_PERIOD - 2) {
            return 50.0; // 충분한 데이터가 없을 때 중간값 반환
        }
        
        // %K 값 3개 수집
        List<Double> kValues = new ArrayList<>();
        
        for (int i = index - STOCH_D_PERIOD + 1; i <= index; i++) {
            double kValue = calculateStochRSI_K(series, i);
            kValues.add(kValue);
        }
        
        // %D = %K의 3기간 평균
        return kValues.stream().mapToDouble(d -> d).average().orElse(50.0);
    }
    
    /**
     * StochRSI 계산 (기존 메서드와의 호환성을 위해 유지)
     * @deprecated calculateStochRSI_K() 또는 calculateStochRSI_D() 사용 권장
     */
    @Deprecated
    public double calculateStochRSI(BarSeries series, int index) {
        return calculateStochRSI_K(series, index);
    }
    
    public TradingSignal calculateSignals(String market, User user) {
        BarSeries series = createBarSeries(market, user);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        
        // 이동평균선 (3일, 10일)
        SMAIndicator smaShort = new SMAIndicator(closePrice, SHORT_SMA);
        SMAIndicator smaLong = new SMAIndicator(closePrice, LONG_SMA);
        
        // MACD (6, 13, 5)
        MACDIndicator macd = new MACDIndicator(closePrice, MACD_SHORT, MACD_LONG);
        SMAIndicator signal = new SMAIndicator(macd, MACD_SIGNAL);
        
        // RSI (9일)
        RSIIndicator rsi = new RSIIndicator(closePrice, RSI_PERIOD);
        
        // Bollinger Bands (20일, 1.5σ 사용)
        SMAIndicator smaBB = new SMAIndicator(closePrice, BB_PERIOD);
        StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, BB_PERIOD);
        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(smaBB);
        // 1.5σ를 위해 표준편차 값을 직접 계산하여 적용
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
        
        // RSI 시그널
        boolean rsiOversold = rsi.getValue(lastIndex).isLessThan(series.numOf(RSI_BASE_OVERSOLD));
        boolean rsiOverbought = rsi.getValue(lastIndex).isGreaterThan(series.numOf(RSI_BASE_OVERBOUGHT));
        
        // Stoch RSI 시그널 (개선된 %K 값 사용)
        double stochRsiKValue = calculateStochRSI_K(series, lastIndex);
        double stochRsiDValue = calculateStochRSI_D(series, lastIndex);
        boolean stochRsiOversold = stochRsiKValue <= STOCH_RSI_OVERSOLD;
        boolean stochRsiOverbought = stochRsiKValue >= STOCH_RSI_OVERBOUGHT;
        
        // 디버깅 로그 (개선된 정보 포함)
        if (stochRsiOversold) {
            log.info("Stoch RSI 과매도 시그널! Market: {}, Stoch RSI %K: {}, %D: {}", 
                market, String.format("%.2f", stochRsiKValue), String.format("%.2f", stochRsiDValue));
        }
        if (stochRsiOverbought) {
            log.info("Stoch RSI 과매수 시그널! Market: {}, Stoch RSI %K: {}, %D: {}", 
                market, String.format("%.2f", stochRsiKValue), String.format("%.2f", stochRsiDValue));
        }
        
        // Bollinger Bands 시그널 (기존)
        boolean bbOverSold = closePrice.getValue(lastIndex).isLessThan(bbl.getValue(lastIndex));
        boolean bbOverBought = closePrice.getValue(lastIndex).isGreaterThan(bbu.getValue(lastIndex));
        
        // 볼린저 밴드 평균 회귀 전략 시그널 (새로 추가)
        boolean bbMeanReversionBuySignal = calculateBollingerMeanReversionBuySignal(series, lastIndex);
        boolean bbMeanReversionSellSignal = calculateBollingerMeanReversionSellSignal(series, lastIndex);
        
        // 볼린저 밴드 평균 회귀 시그널 로깅
        if (bbMeanReversionBuySignal) {
            log.info("볼린저 밴드 평균 회귀 매수 시그널! Market: {}, Price: {}, Lower Band: {}", 
                market, closePrice.getValue(lastIndex), bbl.getValue(lastIndex));
        }
        if (bbMeanReversionSellSignal) {
            log.info("볼린저 밴드 평균 회귀 매도 시그널! Market: {}, Price: {}, Middle Band: {}", 
                market, closePrice.getValue(lastIndex), bbm.getValue(lastIndex));
        }
        
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
            new BigDecimal(rsi.getValue(lastIndex).doubleValue()),
            stochRsiOversold,
            stochRsiOverbought,
            new BigDecimal(stochRsiKValue), // %K 값
            new BigDecimal(stochRsiDValue), // %D 값
            bbOverSold,
            bbOverBought,
            bbMeanReversionBuySignal,
            bbMeanReversionSellSignal
        );
    }

    public BigDecimal transformRsiValue(BigDecimal x, BigDecimal baseOrderAmount, BigDecimal maxOrderAmount) {
        if (x.compareTo(BigDecimal.ZERO) <= 0) {
            return maxOrderAmount;
        } else if (x.compareTo(BigDecimal.valueOf(RSI_BASE_OVERSOLD)) <= 0) {
            return baseOrderAmount.subtract(maxOrderAmount)
                .multiply(x)
                .divide(BigDecimal.valueOf(RSI_BASE_OVERSOLD), 8, RoundingMode.HALF_UP)
                .add(maxOrderAmount);
        } else if (x.compareTo(BigDecimal.valueOf(RSI_BASE_OVERBOUGHT)) < 0) {
            return baseOrderAmount;
        } else if (x.compareTo(BigDecimal.valueOf(100)) <= 0) {
            return baseOrderAmount.subtract(maxOrderAmount)
                .multiply(x)
                .add(maxOrderAmount.multiply(BigDecimal.valueOf(RSI_BASE_OVERBOUGHT)))
                .subtract(BigDecimal.valueOf(100).multiply(baseOrderAmount))
                .divide(BigDecimal.valueOf(RSI_BASE_OVERBOUGHT).subtract(BigDecimal.valueOf(100)), 8, RoundingMode.HALF_UP);
        } else {
            return maxOrderAmount;
        }
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
} 