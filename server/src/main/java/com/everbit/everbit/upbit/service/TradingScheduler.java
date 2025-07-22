package com.everbit.everbit.upbit.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import com.everbit.everbit.upbit.dto.trading.TradingSignal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("prod")  // prod 프로필에서만 활성화
public class TradingScheduler {
    private final TradingSignalService tradingSignalService;
    private final UpbitExchangeClient upbitExchangeClient;
    
    private static final String[] MARKETS = {"KRW-BTC"}; // 거래할 마켓 목록
    
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void checkTradingSignals() {
        for (String market : MARKETS) {
            try {
                TradingSignal signal = tradingSignalService.calculateSignals(market);
                processSignal(signal);
            } catch (Exception e) {
                log.error("Failed to process trading signal for market: {}", market, e);
            }
        }
    }
    
    private void processSignal(TradingSignal signal) {
        String market = signal.market();
        
        if (signal.isBuySignal()) {
            log.info("Buy signal detected for {}", market);
            // TODO: 매수 주문 로직 구현
            // 1. 계좌 잔고 확인
            // 2. 주문 가능 수량 계산
            // 3. 주문 실행
            // 4. 주문 결과 저장
        }
        
        if (signal.isSellSignal()) {
            log.info("Sell signal detected for {}", market);
            // TODO: 매도 주문 로직 구현
            // 1. 보유 수량 확인
            // 2. 주문 실행
            // 3. 주문 결과 저장
        }
    }
} 