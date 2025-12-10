package com.everbit.everbit.upbit.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.everbit.everbit.trade.entity.Trade;
import com.everbit.everbit.trade.entity.enums.Strategy;
import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.trade.service.TradeService;
import com.everbit.everbit.upbit.dto.exchange.AccountResponse;
import com.everbit.everbit.upbit.dto.exchange.OrderRequest;
import com.everbit.everbit.upbit.dto.exchange.OrderResponse;
import com.everbit.everbit.upbit.dto.quotation.TickerResponse;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.upbit.repository.CustomSignalRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 손실 및 이익 관리 비즈니스 로직을 처리하는 서비스
 * Spring Batch에서 사용하기 위해 비즈니스 로직을 분리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LossAndProfitManagementService {
    private final UpbitExchangeClient upbitExchangeClient;
    private final UpbitQuotationClient upbitQuotationClient;
    private final TradeService tradeService;
    private final CustomSignalRepository customSignalRepository;

    /**
     * 사용자의 손실 및 이익 관리를 처리합니다.
     * 
     * @param user 처리할 사용자
     * @return 처리 결과 (성공 여부)
     */
    @Transactional
    public boolean processUserLossAndProfitManagement(User user) {
        try {
            // 전체 계좌 잔고 조회
            List<AccountResponse> accounts = upbitExchangeClient.getAccounts(user.getUsername());
            
            // 사용자의 botSettings에서 마켓 목록 가져오기
            for (Market market : user.getBotSetting().getMarketList()) {
                try {
                    AccountResponse account = accounts.stream()
                        .filter(a -> a.currency().equals(market.name()))
                        .findFirst()
                        .orElse(null);
                    if (account == null || new BigDecimal(account.balance()).compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
        
                    log.info("사용자: {}, 마켓: {}, 코인: {} - 손실 및 이익 관리 처리 시작", 
                        user.getUsername(), market.getCode(), account.currency());
                    processMarketLossAndProfitManagement(user, market, account);
                } catch (Exception e) {
                    log.error("사용자: {}, 마켓: {} - 손실 및 이익 관리 처리 실패", user.getUsername(), market.getCode(), e);
                }
            }
            return true;
        } catch (Exception e) {
            log.error("사용자: {} - 계좌 잔고 조회 실패", user.getUsername(), e);
            return false;
        }
    }
    
    private void processMarketLossAndProfitManagement(User user, Market market, AccountResponse account) {
        BigDecimal coinBalance = new BigDecimal(account.balance());
        BigDecimal avgBuyPrice = new BigDecimal(account.avgBuyPrice());
        
        if (avgBuyPrice.compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("사용자: {}, 마켓: {} - 매수 평균가가 0 이하", user.getUsername(), market);
            return;
        }
        
        // 현재 가격 조회
        BigDecimal currentPrice = getCurrentPrice(market);
        
        // 수익률 계산 (양수: 이익, 음수: 손실)
        BigDecimal profitRate = calculateProfitRate(currentPrice, avgBuyPrice);
        log.info("사용자: {}, 마켓: {} - 보유수량: {}, 평균매수가: {}, 현재가: {}, 수익률: {}%", 
            user.getUsername(), market, coinBalance, avgBuyPrice, currentPrice, 
            profitRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        
        // BotSetting에서 손실/이익 임계값과 비율 가져오기
        BigDecimal lossThreshold = user.getBotSetting().getLossThreshold();
        BigDecimal profitThreshold = user.getBotSetting().getProfitThreshold();
        BigDecimal lossSellRatio = user.getBotSetting().getLossSellRatio();
        BigDecimal profitSellRatio = user.getBotSetting().getProfitSellRatio();

        // 30분 경과 후 0.1% 이익도 못 내면 전량 매도 체크
        checkAndExecuteFullSellIfNeeded(user, market, coinBalance, currentPrice, avgBuyPrice, profitSellRatio);
        
        // 손실 관리가 활성화된 경우에만 손실 임계값 체크
        if (user.getBotSetting().getIsLossManagementActive()) {
            if (profitRate.compareTo(lossThreshold.negate()) <= 0) {
                log.warn("사용자: {}, 마켓: {} - {}% 이상 손실 감지! 부분 매도 실행", 
                    user.getUsername(), market, lossThreshold.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
                executePartialSell(user, market, coinBalance, currentPrice, Strategy.LOSS_MANAGEMENT, lossSellRatio);
                
                // 이상 손실 감지 시 CustomSignal의 연속 하락 카운트 초기화
                // EXTREME_FLIP 전략의 추격 매수를 방지하여 반복 손실을 막음
                resetConsecutiveDropForMarket(market);
            }
        } else {
            log.debug("사용자: {}, 마켓: {} - 손실 관리가 비활성화되어 있음", user.getUsername(), market);
        }
        
        // 이익 관리가 활성화된 경우에만 이익 임계값 체크
        if (user.getBotSetting().getIsProfitTakingActive()) {
            if (profitRate.compareTo(profitThreshold) >= 0) {
                log.info("사용자: {}, 마켓: {} - {}% 이상 이익 감지! 부분 매도 실행", 
                    user.getUsername(), market, profitThreshold.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
                executePartialSell(user, market, coinBalance, currentPrice, Strategy.PROFIT_TAKING, profitSellRatio);
            }
        } else {
            log.debug("사용자: {}, 마켓: {} - 이익 관리가 비활성화되어 있음", user.getUsername(), market);
        }
    }
    
    private BigDecimal getCurrentPrice(Market market) {
        List<TickerResponse> tickers = upbitQuotationClient.getTickers(List.of(market.getCode()));
        TickerResponse ticker = tickers.get(0);
        return new BigDecimal(ticker.tradePrice());
    }
    
    /**
     * TIMEOUT_SELL_MINUTES분 경과 후 TIMEOUT_SELL_PROFIT_RATE 이익도 못 내면 전량 매도하는 메서드
     */
    private void checkAndExecuteFullSellIfNeeded(User user, Market market, BigDecimal coinBalance, 
                                                  BigDecimal currentPrice, BigDecimal avgBuyPrice, BigDecimal sellRatio) {
        try {
            if (!user.getBotSetting().getIsTimeOutSellActive()) {
                log.debug("사용자: {}, 마켓: {} - 시간초과 관리가 비활성화되어 있음", user.getUsername(), market);
                return;
            }
            int timeOutSellMinutes = user.getBotSetting().getTimeOutSellMinutes();
            BigDecimal timeOutSellProfitRatio = user.getBotSetting().getTimeOutSellProfitRatio();
            // 마지막 매수 거래 정보 조회
            Trade lastBuyTrade = tradeService.findLastBuyByUserAndMarket(user, market);
            
            // 마지막 매수 거래로부터 경과 시간 계산 (분 단위)
            LocalDateTime lastBuyTime = lastBuyTrade.getUpdatedAt();
            LocalDateTime now = LocalDateTime.now();
            long minutesElapsed = ChronoUnit.MINUTES.between(lastBuyTime, now);
            
            // TIMEOUT_SELL_MINUTES분이 지났는지 확인
            if (minutesElapsed >= timeOutSellMinutes) {
                // 수익률 계산
                BigDecimal profitRate = calculateProfitRate(currentPrice, avgBuyPrice);
                
                // TIMEOUT_SELL_PROFIT_RATE 이익도 못 낸 경우 부분 매도
                 if (profitRate.compareTo(timeOutSellProfitRatio) < 0) {
                    log.warn("사용자: {}, 마켓: {} - {}분 경과 후 {}% 이익도 못 내서 부분 매도 실행! 경과시간: {}분, 수익률: {}%", 
                        user.getUsername(), market, timeOutSellMinutes, 
                        timeOutSellProfitRatio.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP),
                        minutesElapsed, 
                        profitRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
                     
                    // TIMEOUT_SELL_MINUTES분 경과 후 이익도 못 내면 보유량의 sellRatio 매도
                    executePartialSell(user, market, coinBalance, currentPrice, Strategy.TIMEOUT_SELL, sellRatio);
                 } else {
                    log.info("사용자: {}, 마켓: {} - {}분 경과했지만 {}% 이상 이익이 있어 전량 매도 건너뜀. 경과시간: {}분, 수익률: {}%", 
                        user.getUsername(), market, timeOutSellMinutes, 
                        timeOutSellProfitRatio.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP),
                        minutesElapsed, 
                        profitRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
                }
            } else {
                log.debug("사용자: {}, 마켓: {} - 마지막 매수로부터 {}분 경과 ({}분 미만)", 
                    user.getUsername(), market, minutesElapsed, timeOutSellMinutes);
            }
        } catch (Exception e) {
            log.error("사용자: {}, 마켓: {} - 시간초과 관리 체크 중 오류 발생", user.getUsername(), market, e);
        }
    }
    
    private void executePartialSell(User user, Market market, BigDecimal coinBalance, BigDecimal currentPrice, 
                                    Strategy strategy, BigDecimal sellRatio) {
        try {
            // 사용자의 sellBaseOrderAmount 가져오기
            BigDecimal baseOrderAmount = BigDecimal.valueOf(user.getBotSetting().getSellBaseOrderAmount());
            
            // 지정된 비율로 매도할 수량 계산
            BigDecimal sellQuantity = coinBalance.multiply(sellRatio);
            BigDecimal sellAmount = sellQuantity.multiply(currentPrice);
            
            // 최소주문금액으로 매도 가능한 수량 계산
            BigDecimal minOrderQuantity = baseOrderAmount.divide(currentPrice, 8, RoundingMode.HALF_UP);
            
            // 지정된 비율 수량과 최소주문금액 수량 중 최댓값 선택
            if (sellAmount.compareTo(baseOrderAmount) < 0) {
                sellQuantity = minOrderQuantity;
                log.info("사용자: {}, 마켓: {} - 최소주문금액 기준 매도: {} (금액: {})", 
                    user.getUsername(), market, sellQuantity, baseOrderAmount);
            } else {
                log.info("사용자: {}, 마켓: {} - 잔량의 {}% 매도: {} (금액: {})", 
                    user.getUsername(), market, sellRatio.multiply(new BigDecimal("100")), sellQuantity, sellAmount);
            }
            
            // 매도 수량이 보유 수량보다 크거나, 매도 후 남은 수량의 가치가 최소 주문 금액보다 작으면 전체 수량 매도
            BigDecimal remainingQuantity = coinBalance.subtract(sellQuantity);
            BigDecimal remainingValue = remainingQuantity.multiply(currentPrice);
            
            if (sellQuantity.compareTo(coinBalance) > 0 || remainingValue.compareTo(baseOrderAmount) < 0) {
                sellQuantity = coinBalance;
                log.info("사용자: {}, 마켓: {} - 전체 수량 매도: {} (사유: {})", 
                    user.getUsername(), market, sellQuantity,
                    sellQuantity.compareTo(coinBalance) > 0 ? "매도 수량이 보유 수량보다 큼" : "남은 수량의 가치가 최소 주문 금액보다 작음");
            }
            
            // 시장가 매도 주문 생성
            String sellQuantityStr = sellQuantity.toPlainString();
            OrderRequest orderRequest = OrderRequest.createSellOrder(market.getCode(), sellQuantityStr, currentPrice.toString());
            
            // 매도 주문 실행
            OrderResponse orderResponse = upbitExchangeClient.createOrder(user.getUsername(), orderRequest);
            
            // 주문 결과 저장
            tradeService.saveTrade(user, market.getCode(), strategy, orderResponse, currentPrice);
        } catch (Exception e) {
            log.error("사용자: {}, 마켓: {} - 부분 매도 주문 실패", user.getUsername(), market, e);
        }
    }

    private BigDecimal calculateProfitRate(BigDecimal currentPrice, BigDecimal avgBuyPrice) {
        return currentPrice.subtract(avgBuyPrice).divide(avgBuyPrice, 4, RoundingMode.HALF_UP);
    }
    
    /**
     * 특정 마켓의 CustomSignal에서 연속 하락 카운트를 초기화합니다.
     * 이상 손실이 감지되었을 때 EXTREME_FLIP 전략의 추격 매수를 방지하여 반복 손실을 막기 위해 사용됩니다.
     */
    private void resetConsecutiveDropForMarket(Market market) {
        try {
            customSignalRepository.findByMarket(market)
                .ifPresentOrElse(
                    customSignal -> {
                        customSignal.resetConsecutiveDrop();
                        customSignalRepository.save(customSignal);
                        log.info("마켓: {} - 연속 하락 카운트 초기화 완료 (이상 손실 방지)", market);
                    },
                    () -> log.debug("마켓: {} - CustomSignal이 존재하지 않아 초기화 건너뜀", market)
                );
        } catch (Exception e) {
            log.error("마켓: {} - 연속 하락 카운트 초기화 중 오류 발생", market, e);
        }
    }
}

