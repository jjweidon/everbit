package com.everbit.everbit.upbit.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.everbit.everbit.upbit.repository.CustomSignalRepository;
import com.everbit.everbit.upbit.entity.CustomSignal;
import com.everbit.everbit.upbit.dto.trading.TradingSignal;
import com.everbit.everbit.trade.entity.enums.Market;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomSignalService {
    private final CustomSignalRepository customSignalRepository;

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
     * 1. (볼린저밴드 하단 도달 + RSI 과매도) → consecutiveDropCount 증가
     * 2. (볼린저밴드 하단 도달 + MACD 추세 변환 감지) + 최근 10분 내 Drop 있음 → 매수 시그널 발생
     */
    public boolean processDropNFlipSignal(TradingSignal signal, Market market) {
        CustomSignal customSignal = findOrCreateCustomSignal(market);
        
        // 10분 경과 시 초기화 체크
        checkAndResetExpiredSignals(customSignal);
        
        // 조건 1: 볼린저밴드 하단 도달 + RSI 과매도
        boolean bbRsiDropCondition = signal.bbBuySignal() && signal.rsiBuySignal();
        
        // 조건 2: 볼린저밴드 하단 도달 + MACD 추세 변환 감지
        boolean bbMacdDropCondition = signal.bbBuySignal() && signal.macdBuySignal();
        
        boolean buySignalGenerated = false;
        
        if (bbRsiDropCondition) {
            customSignal.countUpConsecutiveDrop();
            customSignalRepository.save(customSignal);
            log.debug("DROP count increased for market {}: {}", market, customSignal.getConsecutiveDropCount());
        }
        
        if (bbMacdDropCondition) {
            customSignal.updateLastFlipUpAt();
            if (customSignal.getConsecutiveDropCount() >= 3 && isWithin10Minutes(customSignal.getLastDropAt())) {
                // 매수 시그널 발생
                buySignalGenerated = true;
                customSignal.resetConsecutiveDrop();
                log.info("DROP_N_FLIP 매수 신호 발생: {}", market);
            }
            customSignalRepository.save(customSignal);
        }
    
        return buySignalGenerated;
    }

    /**
     * POP_N_FLIP 매도 시그널 감지 및 처리
     * 1. (볼린저밴드 상단 도달 + RSI 과매수) → consecutivePopCount 증가
     * 2. (볼린저밴드 상단 도달 + MACD 추세 변환 감지) + 최근 10분 내 Pop 있음 → 매도 시그널 발생
     */
    public boolean processPopNFlipSignal(TradingSignal signal, Market market) {
        CustomSignal customSignal = findOrCreateCustomSignal(market);
        
        // 10분 경과 시 초기화 체크
        checkAndResetExpiredSignals(customSignal);
        
        // 조건 1: 볼린저밴드 상단 도달 + RSI 과매수
        boolean bbRsiPopCondition = signal.bbSellSignal() && signal.rsiSellSignal();
        
        // 조건 2: 볼린저밴드 상단 도달 + MACD 추세 변환 감지
        boolean bbMacdPopCondition = signal.bbSellSignal() && signal.macdSellSignal();
        
        boolean sellSignalGenerated = false;
        
        if (bbRsiPopCondition) {
            customSignal.countUpConsecutivePop();
            customSignalRepository.save(customSignal);
            log.debug("POP count increased for market {}: {}", market, customSignal.getConsecutivePopCount());
        }

        if (bbMacdPopCondition) {
            customSignal.updateLastFlipDownAt();
            if (customSignal.getConsecutivePopCount() >= 3 && isWithin10Minutes(customSignal.getLastPopAt())) {
                // 매도 시그널 발생
                sellSignalGenerated = true;
                customSignal.resetConsecutivePop();
                log.info("POP_N_FLIP 매수 신호 발생: {}", market);
            }
            customSignalRepository.save(customSignal);
        }

        return sellSignalGenerated;
    }

    /**
     * DROP_N_FLIP 시그널 강도 계산
     * 최소(1개) → 0.00, 최대(5개 이상) → 1.00
     */
    public double calculateDropNFlipSignalStrength(Market market) {
        CustomSignal customSignal = findOrCreateCustomSignal(market);
        int dropCount = customSignal.getConsecutiveDropCount();
        
        if (dropCount <= 0) return 0.0;
        if (dropCount >= 7) return 1.0;
        
        // 1개 → 0.0, 2개 → 0.167, 3개 → 0.333, 4개 → 0.5, 5개 → 0.667, 6개 → 0.833, 7개 이상 → 1.0
        return (dropCount - 1) / 6.0;
    }

    /**
     * POP_N_FLIP 시그널 강도 계산
     * 최소(1개) → 0.00, 최대(5개 이상) → 1.00
     */
    public double calculatePopNFlipSignalStrength(Market market) {
        CustomSignal customSignal = findOrCreateCustomSignal(market);
        int popCount = customSignal.getConsecutivePopCount();
        
        if (popCount <= 0) return 0.0;
        if (popCount >= 7) return 1.0;
        
        // 1개 → 0.0, 2개 → 0.167, 3개 → 0.333, 4개 → 0.5, 5개 → 0.667, 6개 → 0.833, 7개 이상 → 1.0
        return (popCount - 1) / 6.0;
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
