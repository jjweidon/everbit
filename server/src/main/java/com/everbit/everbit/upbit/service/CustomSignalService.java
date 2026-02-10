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
    
    // 멀티 타임프레임 컨펌 활성화 여부 (선택적 기능)
    private static final boolean ENABLE_MULTI_TIMEFRAME = false; // 기본값: 비활성화

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
     * DROP_N_FLIP 매수 시그널 감지 및 처리 (v2.0 - 최적화된 버전)
     * 
     * 변경 사항:
     * 1. 멀티 오실레이터 조합 (RSI + Stochastic)
     * 2. 거래량 급증 확인 추가
     * 3. 동적 RSI 임계값 조정 (시장 변동성 기반)
     * 4. 향상된 신호 강도 계산
     * 5. 거래량 기반 카운트 가중치
     */
    public SignalResult processDropNFlipSignal(TradingSignal signal, Market market) {
        CustomSignal customSignal = findOrCreateCustomSignal(market);
        
        // ========== STAGE 1: 추세 및 구조 필터 (기존 유지) ==========
        if (isStrongDowntrend(signal)) {
            log.debug("강한 하락 추세 감지 - 매수 금지: {} (ADX: {}, -DI: {}, +DI: {})", 
                market, signal.adxValue().doubleValue(), 
                signal.minusDI().doubleValue(), signal.plusDI().doubleValue());
            checkAndResetExpiredSignals(customSignal);
            return SignalResult.of(false, 0.0);
        }
        
        if (isDowntrendEMAStructure(signal)) {
            log.debug("하락 추세 EMA 구조 감지 - 매수 금지: {} (EMA20: {}, EMA60: {}, EMA120: {})", 
                market, signal.ema20().doubleValue(), 
                signal.ema60().doubleValue(), signal.ema120().doubleValue());
            checkAndResetExpiredSignals(customSignal);
            return SignalResult.of(false, 0.0);
        }
        
        // ========== STAGE 2: 변동성 확인 (기존 유지) ==========
        BarSeries series = candleDataService.createBarSeries(market.getCode());
        if (!isATRDecreasing(series, signal, customSignal)) {
            log.debug("ATR 증가 중 - 바닥 아님: {} (현재 ATR: {})", 
                market, signal.atrValue().doubleValue());
            checkAndResetExpiredSignals(customSignal);
            return SignalResult.of(false, 0.0);
        }
        
        // ========== STAGE 3: 다중 오실레이터 과매도 확인 (신규) ==========
        // 동적 RSI 임계값 계산 (변동성 기반)
        double dynamicRSIThreshold = calculateDynamicRSIThreshold(series, signal, true);
        
        // 기본 과매도 조건: RSI + Bollinger Bands
        boolean rsiDropCondition = signal.rsiBuySignal() && signal.bbBuySignal();
        
        // 추가 오실레이터 확인 (Stochastic 추가)
        boolean stochasticOversold = checkStochasticOversold(signal);
        
        // 조합 조건: RSI+BB 기본, Stochastic은 선택적 강화
        boolean multiOscillatorCondition = rsiDropCondition || 
            (signal.rsiValue().doubleValue() < dynamicRSIThreshold && stochasticOversold);
        
        // ========== STAGE 4: 거래량 분석 (신규) ==========
        boolean volumeSurge = checkVolumeSurge(series, 1.3); // 평균의 1.3배 이상
        
        // ========== STAGE 5: Divergence 확인 (기존 유지) ==========
        boolean bullishDivergence = checkBullishDivergence(series, signal);
        
        // ========== STAGE 6: 신호 누적 로직 (개선) ==========
        boolean buySignalGenerated = false;
        double signalStrength = 0.0;
        
        // 과매도 신호 발생 시 카운트 증가
        if (multiOscillatorCondition) {
            // 거래량 급증이 있으면 가중치 2배
            int countIncrement = volumeSurge ? 2 : 1;
            for (int i = 0; i < countIncrement; i++) {
                customSignal.countUpConsecutiveDrop();
            }
            customSignalRepository.save(customSignal);
            
            log.debug("DROP count increased: {} (증가량: {}, 총: {}, Volume Surge: {}, Stochastic: {})", 
                market, countIncrement, customSignal.getConsecutiveDropCount(), 
                volumeSurge, stochasticOversold);
        }
        
        // ========== STAGE 7: 진입 트리거 (개선) ==========
        boolean rsiCrossedAbove30 = checkRSICrossAbove30(series, signal);
        
        // 멀티 타임프레임 컨펌 (선택적 - 고급 모드)
        boolean multiTimeframeConfirm = true; // 기본값 true
        if (ENABLE_MULTI_TIMEFRAME) {
            multiTimeframeConfirm = checkHigherTimeframeOversold(market);
        }
        
        // 최종 진입 조건 조합
        boolean entryCondition = customSignal.getConsecutiveDropCount() >= MIN_CONSECUTIVE_COUNT 
            && isWithin10Minutes(customSignal.getLastDropAt())
            && multiTimeframeConfirm
            && (rsiCrossedAbove30 || bullishDivergence || (stochasticOversold && volumeSurge));
        
        if (entryCondition) {
            buySignalGenerated = true;
            
            // 신호 강도 계산 (개선: 더 많은 요소 반영)
            signalStrength = calculateEnhancedSignalStrength(
                market, 
                bullishDivergence, 
                volumeSurge, 
                stochasticOversold,
                customSignal.getConsecutiveDropCount()
            );
            
            customSignal.updateLastFlipUpAt();
            customSignal.setConsecutiveDropCountMin(MIN_CONSECUTIVE_COUNT);
            customSignalRepository.save(customSignal);
            
            log.info("🚀 DROP_N_FLIP v2 매수 신호: {} | 강도: {:.2f} | 카운트: {} | Div: {} | Vol: {} | Stoch: {} | MTF: {}", 
                market, String.format("%.2f", signalStrength), customSignal.getConsecutiveDropCount(), 
                bullishDivergence, volumeSurge, stochasticOversold, multiTimeframeConfirm);
        }
        else {
            checkAndResetExpiredSignals(customSignal);
        }
        
        return SignalResult.of(buySignalGenerated, signalStrength);
    }

    /**
     * POP_N_FLIP 매도 시그널 감지 및 처리 (v2.0 - 최적화된 버전)
     * DROP_N_FLIP v2와 동일한 개선 사항 적용
     */
    public SignalResult processPopNFlipSignal(TradingSignal signal, Market market) {
        CustomSignal customSignal = findOrCreateCustomSignal(market);
        
        // ========== STAGE 1: 추세 필터 (기존 유지) ==========
        if (isStrongUptrend(signal)) {
            log.debug("강한 상승 추세 감지 - 매도 금지: {} (ADX: {}, +DI: {}, -DI: {})", 
                market, signal.adxValue().doubleValue(), 
                signal.plusDI().doubleValue(), signal.minusDI().doubleValue());
            checkAndResetExpiredSignals(customSignal);
            return SignalResult.of(false, 0.0);
        }
        
        if (isUptrendEMAStructure(signal)) {
            log.debug("상승 추세 EMA 구조 감지 - 매도 금지: {} (EMA20: {}, EMA60: {}, EMA120: {})", 
                market, signal.ema20().doubleValue(), 
                signal.ema60().doubleValue(), signal.ema120().doubleValue());
            checkAndResetExpiredSignals(customSignal);
            return SignalResult.of(false, 0.0);
        }
        
        // ========== STAGE 2: 변동성 확인 (기존 유지) ==========
        BarSeries series = candleDataService.createBarSeries(market.getCode());
        if (!isATRDecreasing(series, signal, customSignal)) {
            log.debug("ATR 증가 중 - 고점 아님: {} (현재 ATR: {})", 
                market, signal.atrValue().doubleValue());
            checkAndResetExpiredSignals(customSignal);
            return SignalResult.of(false, 0.0);
        }
        
        // ========== STAGE 3: 다중 오실레이터 과매수 확인 (신규) ==========
        // 동적 RSI 임계값 계산 (변동성 기반)
        double dynamicRSIThreshold = calculateDynamicRSIThreshold(series, signal, false);
        
        // 기본 과매수 조건: RSI + Bollinger Bands
        boolean rsiPopCondition = signal.rsiSellSignal() && signal.bbSellSignal();
        
        // 추가 오실레이터 확인 (Stochastic 추가)
        boolean stochasticOverbought = checkStochasticOverbought(signal);
        
        // 조합 조건: RSI+BB 기본, Stochastic은 선택적 강화
        boolean multiOscillatorCondition = rsiPopCondition || 
            (signal.rsiValue().doubleValue() > dynamicRSIThreshold && stochasticOverbought);
        
        // ========== STAGE 4: 거래량 분석 (신규) ==========
        boolean volumeSurge = checkVolumeSurge(series, 1.3); // 평균의 1.3배 이상
        
        // ========== STAGE 5: Divergence 확인 (기존 유지) ==========
        boolean bearishDivergence = checkBearishDivergence(series, signal);
        
        // ========== STAGE 6: 신호 누적 로직 (개선) ==========
        boolean sellSignalGenerated = false;
        double signalStrength = 0.0;
        
        // 과매수 신호 발생 시 카운트 증가
        if (multiOscillatorCondition) {
            // 거래량 급증이 있으면 가중치 2배
            int countIncrement = volumeSurge ? 2 : 1;
            for (int i = 0; i < countIncrement; i++) {
                customSignal.countUpConsecutivePop();
            }
            customSignalRepository.save(customSignal);
            
            log.debug("POP count increased: {} (증가량: {}, 총: {}, Volume Surge: {}, Stochastic: {})", 
                market, countIncrement, customSignal.getConsecutivePopCount(), 
                volumeSurge, stochasticOverbought);
        }
        
        // ========== STAGE 7: 진입 트리거 (개선) ==========
        boolean rsiCrossedBelow70 = checkRSICrossBelow70(series, signal);
        
        // 멀티 타임프레임 컨펌 (선택적 - 고급 모드)
        boolean multiTimeframeConfirm = true; // 기본값 true
        if (ENABLE_MULTI_TIMEFRAME) {
            multiTimeframeConfirm = checkHigherTimeframeOverbought(market);
        }
        
        // 최종 진입 조건 조합
        boolean entryCondition = customSignal.getConsecutivePopCount() >= MIN_CONSECUTIVE_COUNT 
            && isWithin10Minutes(customSignal.getLastPopAt())
            && multiTimeframeConfirm
            && (rsiCrossedBelow70 || bearishDivergence || (stochasticOverbought && volumeSurge));
        
        if (entryCondition) {
            sellSignalGenerated = true;
            
            // 신호 강도 계산 (개선: 더 많은 요소 반영)
            signalStrength = calculateEnhancedSignalStrength(
                market, 
                bearishDivergence, 
                volumeSurge, 
                stochasticOverbought,
                customSignal.getConsecutivePopCount()
            );
            
            customSignal.updateLastFlipDownAt();
            customSignal.setConsecutivePopCountMin(MIN_CONSECUTIVE_COUNT);
            customSignalRepository.save(customSignal);
            
            log.info("🔻 POP_N_FLIP v2 매도 신호: {} | 강도: {:.2f} | 카운트: {} | Div: {} | Vol: {} | Stoch: {} | MTF: {}", 
                market, String.format("%.2f", signalStrength), customSignal.getConsecutivePopCount(), 
                bearishDivergence, volumeSurge, stochasticOverbought, multiTimeframeConfirm);
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
    
    // ========== EXTREME_FLIP v2.0 신규 헬퍼 메서드 ==========
    
    /**
     * Stochastic 과매도 확인
     * %K와 %D 모두 20 이하일 때 과매도로 판단
     */
    private boolean checkStochasticOversold(TradingSignal signal) {
        if (signal.stochasticK() == null || signal.stochasticD() == null) {
            return false;
        }
        double k = signal.stochasticK().doubleValue();
        double d = signal.stochasticD().doubleValue();
        return k < 20.0 && d < 20.0;
    }
    
    /**
     * Stochastic 과매수 확인
     * %K와 %D 모두 80 이상일 때 과매수로 판단
     */
    private boolean checkStochasticOverbought(TradingSignal signal) {
        if (signal.stochasticK() == null || signal.stochasticD() == null) {
            return false;
        }
        double k = signal.stochasticK().doubleValue();
        double d = signal.stochasticD().doubleValue();
        return k > 80.0 && d > 80.0;
    }
    
    /**
     * 거래량 급증 확인
     * 현재 거래량이 최근 20개 봉의 평균 거래량 대비 threshold 배 이상일 때 급증으로 판단
     * @param series BarSeries
     * @param threshold 평균 대비 배수 (예: 1.3 = 평균의 130%)
     */
    private boolean checkVolumeSurge(BarSeries series, double threshold) {
        int barCount = series.getBarCount();
        if (barCount < 20) {
            return false;
        }
        
        // 최근 20개 봉의 평균 거래량 계산
        double avgVolume = 0.0;
        int startIndex = Math.max(0, barCount - 20);
        int endIndex = barCount - 1;
        
        for (int i = startIndex; i < endIndex; i++) {
            avgVolume += series.getBar(i).getVolume().doubleValue();
        }
        avgVolume /= (endIndex - startIndex);
        
        // 현재 봉의 거래량
        double currentVolume = series.getBar(endIndex).getVolume().doubleValue();
        
        return currentVolume > avgVolume * threshold;
    }
    
    /**
     * ATR SMA 계산 (14일 평균 ATR)
     */
    private double calculateATRSMA(BarSeries series, int period) {
        int barCount = series.getBarCount();
        if (barCount < period) {
            return 0.0;
        }
        
        double sum = 0.0;
        int startIndex = Math.max(0, barCount - period);
        int endIndex = barCount - 1;
        
        for (int i = startIndex; i <= endIndex; i++) {
            // ATR 값을 직접 계산하기 어려우므로, 각 봉의 변동성(high-low)을 사용
            double high = series.getBar(i).getHighPrice().doubleValue();
            double low = series.getBar(i).getLowPrice().doubleValue();
            sum += (high - low);
        }
        
        return sum / (endIndex - startIndex + 1);
    }
    
    /**
     * 동적 RSI 임계값 계산 (변동성 기반)
     * ATR이 높을수록 (변동성 클수록) 임계값을 완화
     * @param series BarSeries
     * @param signal TradingSignal
     * @param isBuy 매수인지 여부 (true: 매수, false: 매도)
     */
    private double calculateDynamicRSIThreshold(BarSeries series, TradingSignal signal, boolean isBuy) {
        double baseThreshold = isBuy ? 30.0 : 70.0;
        double atr = signal.atrValue().doubleValue();
        double atrSMA = calculateATRSMA(series, 14);
        
        if (atrSMA == 0) {
            return baseThreshold; // ATR SMA가 0이면 기본값 반환
        }
        
        if (atr > atrSMA * 1.5) {
            // 변동성 높음: 임계값 완화 (더 쉽게 신호 발생)
            return isBuy ? 35.0 : 65.0;
        } else if (atr < atrSMA * 0.7) {
            // 변동성 낮음: 임계값 엄격화
            return isBuy ? 25.0 : 75.0;
        }
        
        return baseThreshold;
    }
    
    /**
     * ATR 감소 정도 점수 계산 (0.0 ~ 1.0)
     * ATR이 더 많이 감소했을수록 바닥에 가까움
     */
    private double calculateATRDecreaseScore(Market market) {
        CustomSignal customSignal = findOrCreateCustomSignal(market);
        Double previousATR = customSignal.getPreviousAtr();
        
        if (previousATR == null || previousATR == 0) {
            return 0.0;
        }
        
        BarSeries series = candleDataService.createBarSeries(market.getCode());
        int currentIndex = series.getEndIndex();
        
        if (currentIndex < 14) {
            return 0.0;
        }
        
        // 현재 ATR 계산 (간단히 최근 봉들의 평균 변동성 사용)
        double currentATR = 0.0;
        for (int i = Math.max(0, currentIndex - 13); i <= currentIndex; i++) {
            double high = series.getBar(i).getHighPrice().doubleValue();
            double low = series.getBar(i).getLowPrice().doubleValue();
            currentATR += (high - low);
        }
        currentATR /= 14;
        
        // ATR 감소 비율 계산
        double decreaseRatio = currentATR / previousATR;
        
        // 감소 비율이 낮을수록 (더 많이 감소했을수록) 점수가 높음
        // 0.95 이상: 0점, 0.85 이하: 1.0점
        if (decreaseRatio >= 0.95) {
            return 0.0;
        } else if (decreaseRatio <= 0.85) {
            return 1.0;
        } else {
            // 선형 보간: (0.95 - decreaseRatio) / 0.1
            return (0.95 - decreaseRatio) / 0.1;
        }
    }
    
    /**
     * 향상된 신호 강도 계산 (v2.0)
     * 0.0 ~ 1.0 범위, 여러 요소를 종합적으로 반영
     */
    private double calculateEnhancedSignalStrength(
        Market market,
        boolean divergence,  // Bullish 또는 Bearish Divergence
        boolean volumeSurge,
        boolean stochasticConfirm,  // Stochastic 과매도/과매수 확인
        int consecutiveCount
    ) {
        double strength = 0.0;
        
        // 기본 강도: 연속 카운트 (최대 0.3)
        strength += Math.min(consecutiveCount / (double)MIN_CONSECUTIVE_COUNT * 0.3, 0.3);
        
        // Divergence: +0.3
        if (divergence) {
            strength += 0.3;
        }
        
        // 거래량 급증: +0.2
        if (volumeSurge) {
            strength += 0.2;
        }
        
        // Stochastic 확인: +0.15
        if (stochasticConfirm) {
            strength += 0.15;
        }
        
        // ATR 감소 정도에 따른 추가 점수 (최대 0.05)
        strength += calculateATRDecreaseScore(market) * 0.05;
        
        return Math.min(strength, 1.0);
    }
    
    /**
     * 상위 타임프레임 과매도 확인 (4시간봉)
     * 멀티 타임프레임 컨펌용 (현재는 기본값 true 반환)
     */
    private boolean checkHigherTimeframeOversold(Market market) {
        // TODO: TradingSignalRepository가 필요하면 구현
        // 현재는 기본값 true 반환 (컨펌 없이 통과)
        return true;
    }
    
    /**
     * 상위 타임프레임 과매수 확인 (4시간봉)
     * 멀티 타임프레임 컨펌용 (현재는 기본값 true 반환)
     */
    private boolean checkHigherTimeframeOverbought(Market market) {
        // TODO: TradingSignalRepository가 필요하면 구현
        // 현재는 기본값 true 반환 (컨펌 없이 통과)
        return true;
    }
}
