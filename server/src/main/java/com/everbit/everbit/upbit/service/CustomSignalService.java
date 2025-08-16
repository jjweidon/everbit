package com.everbit.everbit.upbit.service;

import org.springframework.stereotype.Service;

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

    private final int MIN_CONSECUTIVE_COUNT = 10;
    private final int MAX_CONSECUTIVE_COUNT = 25;

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
     * DROP_N_FLIP 매수 시그널 감지 및 처리
     * 시그널 강도를 포함한 결과를 반환합니다.
     */
    public SignalResult processDropNFlipSignal(TradingSignal signal, Market market) {
        CustomSignal customSignal = findOrCreateCustomSignal(market);
        
        // RSI 과매도
        boolean rsiDropCondition = signal.rsiBuySignal() && signal.bbBuySignal();
        boolean buySignalGenerated = false;
        double signalStrength = 0.0;
        
        if (rsiDropCondition) {
            customSignal.countUpConsecutiveDrop();
            customSignalRepository.save(customSignal);
            log.debug("DROP count increased for market {}: {}", market, customSignal.getConsecutiveDropCount());
        }
        
        // 추세전환 판정: 3회 이상 연속된 RSI 과매도 신호가 누적되어 있고, 
        // 마지막 RSI 시그널이 10분 이내 발생했으며, 현재 RSI 시그널이 없으면 추세전환
        else if (customSignal.getConsecutiveDropCount() >= MIN_CONSECUTIVE_COUNT && isWithin10Minutes(customSignal.getLastDropAt())) {
            customSignal.updateLastFlipUpAt();
            customSignalRepository.save(customSignal);
            // 매수 시그널 발생
            buySignalGenerated = true;
            signalStrength = calculateDropNFlipSignalStrength(market);
            log.info("DROP_N_FLIP 매수 신호 발생: {} (강도: {}, 카운트: {})", market, signalStrength, customSignal.getConsecutiveDropCount());
        }

        else {
            checkAndResetExpiredSignals(customSignal);
        }
        
        return SignalResult.of(buySignalGenerated, signalStrength);
    }

    /**
     * POP_N_FLIP 매도 시그널 감지 및 처리
     * 시그널 강도를 포함한 결과를 반환합니다.
     */
    public SignalResult processPopNFlipSignal(TradingSignal signal, Market market) {
        CustomSignal customSignal = findOrCreateCustomSignal(market);
        
        // RSI 과매수
        boolean rsiPopCondition = signal.rsiSellSignal() && signal.bbSellSignal();
        boolean sellSignalGenerated = false;
        double signalStrength = 0.0;
        
        if (rsiPopCondition) {
            customSignal.countUpConsecutivePop();
            customSignalRepository.save(customSignal);
            log.debug("POP count increased for market {}: {}", market, customSignal.getConsecutivePopCount());
        }

        // 추세전환 판정: 3회 이상 연속된 RSI 과매수 신호가 누적되어 있고, 
        // 마지막 RSI 시그널이 10분 이내 발생했으며, 현재 RSI 시그널이 없으면 추세전환
        else if (customSignal.getConsecutivePopCount() >= MIN_CONSECUTIVE_COUNT && isWithin10Minutes(customSignal.getLastPopAt())) {
            customSignal.updateLastFlipDownAt();
            customSignalRepository.save(customSignal);
            // 매도 시그널 발생
            sellSignalGenerated = true;
            signalStrength = calculatePopNFlipSignalStrength(market);
            log.info("POP_N_FLIP 매도 신호 발생: {} (강도: {}, 카운트: {})", market, signalStrength, customSignal.getConsecutivePopCount());
        }

        else {
            checkAndResetExpiredSignals(customSignal);
        }
        
        return SignalResult.of(sellSignalGenerated, signalStrength);
    }

    /**
     * DROP_N_FLIP 시그널 강도 계산
     * 최소 → 0.00, 최대 → 1.00
     */
    public double calculateDropNFlipSignalStrength(Market market) {
        CustomSignal customSignal = findOrCreateCustomSignal(market);
        int dropCount = customSignal.getConsecutiveDropCount();
        
        if (dropCount <= MIN_CONSECUTIVE_COUNT) return 0.0;
        if (dropCount >= MAX_CONSECUTIVE_COUNT) return 1.0;
        
        return (dropCount - MIN_CONSECUTIVE_COUNT) / (MAX_CONSECUTIVE_COUNT - MIN_CONSECUTIVE_COUNT);
    }

    /**
     * POP_N_FLIP 시그널 강도 계산
     * 최소 → 0.00, 최대 → 1.00
     */
    public double calculatePopNFlipSignalStrength(Market market) {
        CustomSignal customSignal = findOrCreateCustomSignal(market);
        int popCount = customSignal.getConsecutivePopCount();
        
        if (popCount <= MIN_CONSECUTIVE_COUNT) return 0.0;
        if (popCount >= MAX_CONSECUTIVE_COUNT) return 1.0;
        
        return (popCount - MIN_CONSECUTIVE_COUNT) / (MAX_CONSECUTIVE_COUNT - MIN_CONSECUTIVE_COUNT);
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
