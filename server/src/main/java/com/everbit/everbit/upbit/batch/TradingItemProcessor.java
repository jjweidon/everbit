package com.everbit.everbit.upbit.batch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.upbit.batch.dto.UserMarketPair;
import com.everbit.everbit.upbit.dto.trading.TradingSignal;
import com.everbit.everbit.upbit.service.TradingSignalService;
import com.everbit.everbit.upbit.service.TradingService;
import com.everbit.everbit.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradingItemProcessor implements ItemProcessor<UserMarketPair, UserMarketPair> {
    
    private final TradingSignalService tradingSignalService;
    private final TradingService tradingService;

    @Override
    public UserMarketPair process(UserMarketPair pair) throws Exception {
        User user = pair.getUser();
        Market market = pair.getMarket();
        
        try {
            log.info("사용자: {}, 마켓: {} - 트레이딩 시그널 처리 시작", user.getUsername(), market);
            
            // 마켓 시그널 계산
            TradingSignal signal = tradingSignalService.calculateBasicSignals(market.getCode());
            
            if (signal == null) {
                log.warn("사용자: {}, 마켓: {} - 시그널 계산 실패", user.getUsername(), market);
                return pair;
            }
            
            // 시그널 처리
            boolean processed = tradingService.processSignal(signal, user);
            
            if (processed) {
                log.info("사용자: {}, 마켓: {} - 트레이딩 시그널 처리 완료", user.getUsername(), market);
            } else {
                log.debug("사용자: {}, 마켓: {} - 트레이딩 시그널 없음", user.getUsername(), market);
            }
            
            return pair;
        } catch (Exception e) {
            log.error("사용자: {}, 마켓: {} - 트레이딩 시그널 처리 실패", user.getUsername(), market, e);
            return pair;
        }
    }
}
