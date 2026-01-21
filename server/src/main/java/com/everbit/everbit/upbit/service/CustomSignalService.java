package com.everbit.everbit.upbit.service;

import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.everbit.everbit.upbit.repository.CustomSignalRepository;
import com.everbit.everbit.upbit.entity.CustomSignal;
import com.everbit.everbit.upbit.dto.trading.TradingSignal;
import com.everbit.everbit.upbit.dto.trading.SignalResult;
import com.everbit.everbit.trade.entity.enums.Market;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomSignalService {
    private final CustomSignalRepository customSignalRepository;
    private final CandleDataService candleDataService;

    private final int MIN_CONSECUTIVE_COUNT = 10;
    private final int MAX_CONSECUTIVE_COUNT = 30;
    
    // 추세 필터 기준값
    private static final double ADX_STRONG_TREND = 25.0; // 강한 추세 기준

    public CustomSignal findOrCreateCustomSignal(Market market) {
        return customSignalRepository.findByMarket(market)
            .orElseGet(() -> {
                CustomSignal marketSignal = CustomSignal.builder()
                    .market(market)
                    .build();
                return customSignalRepository.save(marketSignal);
            });
    }

    /**
     * DROP_N_FLIP 매수 시그널 감지 및 처리 (개선된 버전)
     * 시그널 강도를 포함한 결과를 반환합니다.
     */
    public SignalResult processDropNFlipSignal(TradingSignal signal, Market market) {
        CustomSignal customSignal = findOrCreateCustomSignal(market);
        
        // Step 1: 추세 필터 확인 - 강한 하락 추세 중이면 매수 금지
        if (isStrongDowntrend(signal)) {
            log.debug("강한 하락 추세 감지 - 매수 금지: {} (ADX: {}, -DI: {}, +DI: {})", 
                market, signal.adxValue().doubleValue(), 
                signal.minusDI().doubleValue(), signal.plusDI().doubleValue());
            checkAndResetExpiredSignals(customSignal);
            return SignalResult.of(false, 0.0);
        }
        
        // Step 2: EMA 구조 확인 - 하락 추세 구조면 매수 금지
        if (isDowntrendEMAStructure(signal)) {
            log.debug("하락 추세 EMA 구조 감지 - 매수 금지: {} (EMA20: {}, EMA60: {}, EMA120: {})", 
                market, signal.ema20().doubleValue(), 
                signal.ema60().doubleValue(), signal.ema120().doubleValue());
            // 하락 추세 중에는 카운트를 증가시키지 않음
            checkAndResetExpiredSignals(customSignal);
            return SignalResult.of(false, 0.0);
        }
        
        // Step 3: ATR 감소 확인 - 변동성이 증가 중이면 매수 금지 (바닥 아님)
        BarSeries series = candleDataService.createBarSeries(market.getCode());
        if (!isATRDecreasing(series, signal, customSignal)) {
            log.debug("ATR 증가 중 - 바닥 아님, 매수 금지: {} (현재 ATR: {})", 
                market, signal.atrValue().doubleValue());
            checkAndResetExpiredSignals(customSignal);
            return SignalResult.of(false, 0.0);
        }
        
        // Step 4: RSI 과매도 조건 확인
        boolean rsiDropCondition = signal.rsiBuySignal() && signal.bbBuySignal();
        boolean buySignalGenerated = false;
        double signalStrength = 0.0;
        
        // Step 5: Bullish Divergence 확인 (가격은 신저가인데 RSI는 이전 저점보다 높음)
        boolean bullishDivergence = checkBullishDivergence(series, signal);
        
        if (rsiDropCondition) {
            // RSI 과매도 신호 발생 시 카운트 증가
            // 단, Bullish Divergence가 있으면 더 강한 신호로 간주
            customSignal.countUpConsecutiveDrop();
            customSignalRepository.save(customSignal);
            log.debug("DROP count increased for market {}: {} (Bullish Divergence: {})", 
                market, customSignal.getConsecutiveDropCount(), bullishDivergence);
        }
        
        // Step 6: 진입 트리거 확인
        // 조건: MIN_CONSECUTIVE_COUNT회 이상 연속된 RSI 과매도 신호가 누적되어 있고,
        // 마지막 RSI 시그널이 10분 이내 발생했으며,
        // 현재 RSI가 30을 상향 돌파했거나 (이전에 30 미만이었다가 현재 30 이상),
        // Bullish Divergence가 발생한 경우
        boolean rsiCrossedAbove30 = checkRSICrossAbove30(series, signal);
        
        if (customSignal.getConsecutiveDropCount() >= MIN_CONSECUTIVE_COUNT 
            && isWithin10Minutes(customSignal.getLastDropAt())
            && (rsiCrossedAbove30 || bullishDivergence)) {
            // 매수 시그널 발생
            buySignalGenerated = true;
            signalStrength = calculateDropNFlipSignalStrength(market, bullishDivergence);
            customSignal.updateLastFlipUpAt();
            customSignal.setConsecutiveDropCountMin(MIN_CONSECUTIVE_COUNT);
            customSignalRepository.save(customSignal);
            log.info("DROP_N_FLIP 매수 신호 발생: {} (강도: {}, 카운트: {}, Bullish Divergence: {}, RSI Cross: {})", 
                market, signalStrength, customSignal.getConsecutiveDropCount(), 
                bullishDivergence, rsiCrossedAbove30);
        }

        else {
            checkAndResetExpiredSignals(customSignal);
        }
        
        return SignalResult.of(buySignalGenerated, signalStrength);
    }

    /**
     * POP_N_FLIP 매도 시그널 감지 및 처리 (개선된 버전)
     * DROP_N_FLIP과 동일한 로직으로 추세 필터, 변동성 확인, Bearish Divergence 등을 추가
     * 시그널 강도를 포함한 결과를 반환합니다.
     */
    public SignalResult processPopNFlipSignal(TradingSignal signal, Market market) {
        CustomSignal customSignal = findOrCreateCustomSignal(market);
        
        // Step 1: 추세 필터 확인 - 강한 상승 추세 중이면 매도 금지
        if (isStrongUptrend(signal)) {
            log.debug("강한 상승 추세 감지 - 매도 금지: {} (ADX: {}, +DI: {}, -DI: {})", 
                market, signal.adxValue().doubleValue(), 
                signal.plusDI().doubleValue(), signal.minusDI().doubleValue());
            checkAndResetExpiredSignals(customSignal);
            return SignalResult.of(false, 0.0);
        }
        
        // Step 2: EMA 구조 확인 - 상승 추세 구조면 매도 금지
        if (isUptrendEMAStructure(signal)) {
            log.debug("상승 추세 EMA 구조 감지 - 매도 금지: {} (EMA20: {}, EMA60: {}, EMA120: {})", 
                market, signal.ema20().doubleValue(), 
                signal.ema60().doubleValue(), signal.ema120().doubleValue());
            // 상승 추세 중에는 카운트를 증가시키지 않음
            checkAndResetExpiredSignals(customSignal);
            return SignalResult.of(false, 0.0);
        }
        
        // Step 3: ATR 감소 확인 - 변동성이 증가 중이면 매도 금지 (고점 아님)
        BarSeries series = candleDataService.createBarSeries(market.getCode());
        if (!isATRDecreasing(series, signal, customSignal)) {
            log.debug("ATR 증가 중 - 고점 아님, 매도 금지: {} (현재 ATR: {})", 
                market, signal.atrValue().doubleValue());
            checkAndResetExpiredSignals(customSignal);
            return SignalResult.of(false, 0.0);
        }
        
        // Step 4: RSI 과매수 조건 확인
        boolean rsiPopCondition = signal.rsiSellSignal() && signal.bbSellSignal();
        boolean sellSignalGenerated = false;
        double signalStrength = 0.0;
        
        // Step 5: Bearish Divergence 확인 (가격은 신고가인데 RSI는 이전 고점보다 낮음)
        boolean bearishDivergence = checkBearishDivergence(series, signal);
        
        if (rsiPopCondition) {
            // RSI 과매수 신호 발생 시 카운트 증가
            // 단, Bearish Divergence가 있으면 더 강한 신호로 간주
            customSignal.countUpConsecutivePop();
            customSignalRepository.save(customSignal);
            log.debug("POP count increased for market {}: {} (Bearish Divergence: {})", 
                market, customSignal.getConsecutivePopCount(), bearishDivergence);
        }
        
        // Step 6: 진입 트리거 확인
        // 조건: MIN_CONSECUTIVE_COUNT회 이상 연속된 RSI 과매수 신호가 누적되어 있고,
        // 마지막 RSI 시그널이 10분 이내 발생했으며,
        // 현재 RSI가 70을 하향 돌파했거나 (이전에 70 이상이었다가 현재 70 미만),
        // Bearish Divergence가 발생한 경우
        boolean rsiCrossedBelow70 = checkRSICrossBelow70(series, signal);
        
        if (customSignal.getConsecutivePopCount() >= MIN_CONSECUTIVE_COUNT 
            && isWithin10Minutes(customSignal.getLastPopAt())
            && (rsiCrossedBelow70 || bearishDivergence)) {
            // 매도 시그널 발생
            sellSignalGenerated = true;
            signalStrength = calculatePopNFlipSignalStrength(market, bearishDivergence);
            customSignal.updateLastFlipDownAt();
            customSignal.setConsecutivePopCountMin(MIN_CONSECUTIVE_COUNT);
            customSignalRepository.save(customSignal);
            log.info("POP_N_FLIP 매도 신호 발생: {} (강도: {}, 카운트: {}, Bearish Divergence: {}, RSI Cross: {})", 
                market, signalStrength, customSignal.getConsecutivePopCount(), 
                bearishDivergence, rsiCrossedBelow70);
        }

        else {
            checkAndResetExpiredSignals(customSignal);
        }
        
        return SignalResult.of(sellSignalGenerated, signalStrength);
    }

    /**
     * DROP_N_FLIP 시그널 강도 계산 (개선된 버전)
     * 최소 → 0.00, 최대 → 1.00
     */
    public double calculateDropNFlipSignalStrength(Market market, boolean bullishDivergence) {
        CustomSignal customSignal = findOrCreateCustomSignal(market);
        int dropCount = customSignal.getConsecutiveDropCount();
        
        if (dropCount <= MIN_CONSECUTIVE_COUNT) return 0.0;
        if (dropCount >= MAX_CONSECUTIVE_COUNT) return 1.0;
        
        double baseStrength = (dropCount - MIN_CONSECUTIVE_COUNT) / (double)(MAX_CONSECUTIVE_COUNT - MIN_CONSECUTIVE_COUNT);
        
        // Bullish Divergence가 있으면 강도 20% 증가 (최대 1.0)
        if (bullishDivergence) {
            baseStrength = Math.min(1.0, baseStrength * 1.2);
        }
        
        return baseStrength;
    }
    
    /**
     * 강한 하락 추세 확인
     * ADX > 25 & -DI > +DI → 강한 하락 추세
     */
    private boolean isStrongDowntrend(TradingSignal signal) {
        double adx = signal.adxValue().doubleValue();
        double plusDI = signal.plusDI().doubleValue();
        double minusDI = signal.minusDI().doubleValue();
        
        return adx > ADX_STRONG_TREND && minusDI > plusDI;
    }
    
    /**
     * 하락 추세 EMA 구조 확인
     * EMA 20 < EMA 60 < EMA 120 → 하락 추세 구조
     */
    private boolean isDowntrendEMAStructure(TradingSignal signal) {
        double ema20 = signal.ema20().doubleValue();
        double ema60 = signal.ema60().doubleValue();
        double ema120 = signal.ema120().doubleValue();
        
        // EMA 값이 0이면 아직 계산되지 않음
        if (ema20 == 0 || ema60 == 0 || ema120 == 0) {
            return false;
        }
        
        return ema20 < ema60 && ema60 < ema120;
    }
    
    /**
     * ATR 감소 확인
     * 현재 ATR이 이전 ATR보다 감소했는지 확인 (바닥/고점 형성 신호)
     * CustomSignal에 이전 ATR을 저장하여 비교
     */
    private boolean isATRDecreasing(BarSeries series, TradingSignal signal, CustomSignal customSignal) {
        int currentIndex = series.getEndIndex();
        if (currentIndex < 14) { // ATR 계산에 필요한 최소 기간
            return true; // 데이터 부족 시 허용
        }
        
        double currentATR = signal.atrValue().doubleValue();
        
        // ATR이 0이면 아직 계산되지 않음
        if (currentATR == 0) {
            return true;
        }
        
        // 이전 ATR이 저장되어 있지 않으면 첫 번째 계산이므로 허용
        Double previousATR = customSignal.getPreviousAtr();
        if (previousATR == null || previousATR == 0) {
            // 현재 ATR을 저장하고 다음에 비교할 수 있도록 함
            customSignal.updateAtr(currentATR);
            customSignalRepository.save(customSignal);
            return true; // 첫 번째 계산이므로 허용
        }
        
        // 현재 ATR이 이전 ATR보다 감소했는지 확인 (5% 이상 감소)
        // ATR 감소는 변동성이 줄어들고 있다는 신호 (바닥/고점 형성 가능성)
        double atrDecreaseRatio = currentATR / previousATR;
        boolean isDecreasing = atrDecreaseRatio < 0.95; // 5% 이상 감소
        
        // 현재 ATR을 이전 ATR로 업데이트 (다음 비교를 위해)
        customSignal.updateAtr(currentATR);
        customSignalRepository.save(customSignal);
        
        if (!isDecreasing) {
            log.debug("ATR 증가 중 - 변동성 증가: {} (현재 ATR: {}, 이전 ATR: {}, 비율: {})", 
                customSignal.getMarket(), currentATR, previousATR, atrDecreaseRatio);
        }
        
        return isDecreasing;
    }
    
    /**
     * Bullish Divergence 확인
     * 가격은 신저가인데 RSI는 이전 저점보다 높은 경우
     */
    private boolean checkBullishDivergence(BarSeries series, TradingSignal signal) {
        int currentIndex = series.getEndIndex();
        if (currentIndex < 20) { // 충분한 데이터가 없으면 false
            return false;
        }
        
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        double currentPrice = signal.currentPrice().doubleValue();
        double currentRSI = signal.rsiValue().doubleValue();
        
        // 최근 20개 봉 중에서 가장 낮은 가격 찾기
        double lowestPrice = currentPrice;
        
        int lookbackPeriod = Math.min(20, currentIndex);
        for (int i = currentIndex - lookbackPeriod; i < currentIndex; i++) {
            double price = closePrice.getValue(i).doubleValue();
            if (price < lowestPrice) {
                lowestPrice = price;
            }
        }
        
        // 현재 가격이 최근 최저가 근처이고, RSI가 상대적으로 높으면 Bullish Divergence 가능성
        // 가격은 낮은데 RSI가 25 이상이면 Bullish Divergence 가능성
        if (currentPrice <= lowestPrice * 1.02 && currentRSI > 25) {
            return true;
        }
        
        return false;
    }
    
    /**
     * RSI가 30을 상향 돌파했는지 확인
     * 이전 봉의 RSI가 30 미만이고 현재 RSI가 30 이상인 경우
     */
    private boolean checkRSICrossAbove30(BarSeries series, TradingSignal signal) {
        double currentRSI = signal.rsiValue().doubleValue();
        double previousRSI = signal.previousRSIValue().doubleValue();
        
        // 이전 RSI가 30 미만이고 현재 RSI가 30 이상이면 상향 돌파
        return previousRSI < 30 && currentRSI >= 30;
    }
    
    /**
     * 강한 상승 추세 확인
     * ADX > 25 & +DI > -DI → 강한 상승 추세
     */
    private boolean isStrongUptrend(TradingSignal signal) {
        double adx = signal.adxValue().doubleValue();
        double plusDI = signal.plusDI().doubleValue();
        double minusDI = signal.minusDI().doubleValue();
        
        return adx > ADX_STRONG_TREND && plusDI > minusDI;
    }
    
    /**
     * 상승 추세 EMA 구조 확인
     * EMA 20 > EMA 60 > EMA 120 → 상승 추세 구조
     */
    private boolean isUptrendEMAStructure(TradingSignal signal) {
        double ema20 = signal.ema20().doubleValue();
        double ema60 = signal.ema60().doubleValue();
        double ema120 = signal.ema120().doubleValue();
        
        // EMA 값이 0이면 아직 계산되지 않음
        if (ema20 == 0 || ema60 == 0 || ema120 == 0) {
            return false;
        }
        
        return ema20 > ema60 && ema60 > ema120;
    }
    
    /**
     * Bearish Divergence 확인
     * 가격은 신고가인데 RSI는 이전 고점보다 낮은 경우
     */
    private boolean checkBearishDivergence(BarSeries series, TradingSignal signal) {
        int currentIndex = series.getEndIndex();
        if (currentIndex < 20) { // 충분한 데이터가 없으면 false
            return false;
        }
        
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        double currentPrice = signal.currentPrice().doubleValue();
        double currentRSI = signal.rsiValue().doubleValue();
        
        // 최근 20개 봉 중에서 가장 높은 가격 찾기
        double highestPrice = currentPrice;
        
        int lookbackPeriod = Math.min(20, currentIndex);
        for (int i = currentIndex - lookbackPeriod; i < currentIndex; i++) {
            double price = closePrice.getValue(i).doubleValue();
            if (price > highestPrice) {
                highestPrice = price;
            }
        }
        
        // 현재 가격이 최근 최고가 근처이고, RSI가 상대적으로 낮으면 Bearish Divergence 가능성
        // 가격은 높은데 RSI가 75 이하면 Bearish Divergence 가능성
        if (currentPrice >= highestPrice * 0.98 && currentRSI < 75) {
            return true;
        }
        
        return false;
    }
    
    /**
     * RSI가 70을 하향 돌파했는지 확인
     * 이전 봉의 RSI가 70 이상이고 현재 RSI가 70 미만인 경우
     */
    private boolean checkRSICrossBelow70(BarSeries series, TradingSignal signal) {
        double currentRSI = signal.rsiValue().doubleValue();
        double previousRSI = signal.previousRSIValue().doubleValue();
        
        // 이전 RSI가 70 이상이고 현재 RSI가 70 미만이면 하향 돌파
        return previousRSI >= 70 && currentRSI < 70;
    }

    /**
     * POP_N_FLIP 시그널 강도 계산 (개선된 버전)
     * Bearish Divergence가 있으면 강도 보정
     * 최소 → 0.00, 최대 → 1.00
     */
    public double calculatePopNFlipSignalStrength(Market market, boolean bearishDivergence) {
        CustomSignal customSignal = findOrCreateCustomSignal(market);
        int popCount = customSignal.getConsecutivePopCount();
        
        if (popCount <= MIN_CONSECUTIVE_COUNT) return 0.0;
        if (popCount >= MAX_CONSECUTIVE_COUNT) return 1.0;
        
        double baseStrength = (popCount - MIN_CONSECUTIVE_COUNT) / (double)(MAX_CONSECUTIVE_COUNT - MIN_CONSECUTIVE_COUNT);
        
        // Bearish Divergence가 있으면 강도 20% 증가 (최대 1.0)
        if (bearishDivergence) {
            baseStrength = Math.min(1.0, baseStrength * 1.2);
        }
        
        return baseStrength;
    }

    /**
     * 10분 이내인지 확인
     */
    private boolean isWithin10Minutes(LocalDateTime timestamp) {
        if (timestamp == null) return false;
        return timestamp.isAfter(LocalDateTime.now().minusMinutes(10));
    }

    /**
     * 10분 경과된 시그널들 초기화
     */
    private void checkAndResetExpiredSignals(CustomSignal customSignal) {
        boolean needsSave = false;
        
        // Drop 시그널 만료 체크
        if (customSignal.getLastDropAt() != null && 
            !isWithin10Minutes(customSignal.getLastDropAt())) {
            customSignal.resetConsecutiveDrop();
            needsSave = true;
            log.debug("Reset expired DROP signals for market {}", customSignal.getMarket());
        }
        
        // Pop 시그널 만료 체크
        if (customSignal.getLastPopAt() != null && 
            !isWithin10Minutes(customSignal.getLastPopAt())) {
            customSignal.resetConsecutivePop();
            needsSave = true;
            log.debug("Reset expired POP signals for market {}", customSignal.getMarket());
        }
        
        if (needsSave) {
            customSignalRepository.save(customSignal);
        }
    }
}
