package com.everbit.everbit.upbit.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import com.everbit.everbit.trade.entity.enums.Strategy;
import com.everbit.everbit.trade.service.TradeService;
import com.everbit.everbit.upbit.dto.exchange.AccountResponse;
import com.everbit.everbit.upbit.dto.exchange.OrderRequest;
import com.everbit.everbit.upbit.dto.exchange.OrderResponse;
import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.upbit.dto.quotation.TickerResponse;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("prod")  // prod 프로필에서만 활성화
public class LossManagementScheduler {
    private final UserService userService;
    private final UpbitExchangeClient upbitExchangeClient;
    private final UpbitQuotationClient upbitQuotationClient;
    private final TradeService tradeService;
    
    private static final BigDecimal LOSS_THRESHOLD = new BigDecimal("0.01"); // 1% 손실 임계값
    private static final BigDecimal PROFIT_THRESHOLD = new BigDecimal("0.015"); // 1.5% 이익 임계값

    @Transactional
    @Scheduled(cron = "0 */5 * * * *") // 5분마다 실행
    public void checkLossAndProfitManagement() {
        log.info("손실 및 이익 관리 스케줄러 실행");
        
        try {
            List<User> activeUsers = userService.findUsersWithActiveBots();
            
            for (User user : activeUsers) {
                try {
                    processUserLossAndProfitManagement(user);
                } catch (Exception e) {
                    log.error("사용자: {} - 손실 및 이익 관리 처리 중 오류 발생", user.getUsername(), e);
                }
            }
        } catch (Exception e) {
            log.error("손실 및 이익 관리 스케줄러 실행 중 오류 발생", e);
        }
    }
    
    private void processUserLossAndProfitManagement(User user) {
        try {
            // 전체 계좌 잔고 조회
            List<AccountResponse> accounts = upbitExchangeClient.getAccounts(user.getUsername());
            
            // 사용자의 botSettings에서 마켓 목록 가져오기
            for (Market market : user.getBotSetting().getMarketList()) {
                try {
                    // 디버깅을 위한 로그 추가
                    log.debug("사용자: {}, 마켓: {} - 전체 계좌 목록: {}", user.getUsername(), market.getCode(), 
                        accounts.stream().map(a -> a.currency()).toList());
                    
                    AccountResponse account = accounts.stream()
                        .filter(a -> a.currency().equals(market.name()))
                        .findFirst()
                        .orElse(null);
        
                    if (account == null) {
                        log.debug("사용자: {}, 마켓: {} - 해당 마켓의 계좌 정보를 찾을 수 없음 (찾는 currency: {})", 
                            user.getUsername(), market.getCode(), market.name());
                        continue;
                    }
        
                    log.info("사용자: {}, 마켓: {}, 코인: {} - 손실 및 이익 관리 처리 시작", user.getUsername(), market.getCode(), account.currency());
                    processMarketLossAndProfitManagement(user, market, account);
                } catch (Exception e) {
                    log.error("사용자: {}, 마켓: {} - 손실 및 이익 관리 처리 실패", user.getUsername(), market.getCode(), e);
                }
            }
        } catch (Exception e) {
            log.error("사용자: {} - 계좌 잔고 조회 실패", user.getUsername(), e);
        }
    }
    
    private void processMarketLossAndProfitManagement(User user, Market market, AccountResponse account) {
        if (account == null || new BigDecimal(account.balance()).compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("사용자: {}, 마켓: {} - 보유 수량이 없음", user.getUsername(), market);
            return;
        }
        
        BigDecimal coinBalance = new BigDecimal(account.balance());
        BigDecimal avgBuyPrice = new BigDecimal(account.avgBuyPrice());
        
        if (avgBuyPrice.compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("사용자: {}, 마켓: {} - 매수 평균가가 0 이하", user.getUsername(), market);
            return;
        }
        
        // 현재 가격 조회
        BigDecimal currentPrice = getCurrentPrice(market);
        log.info("코인: {}, 현재가: {}", market.getCode(), currentPrice);
        
        // 손실률 및 이익률 계산
        BigDecimal lossRate = calculateLossRate(avgBuyPrice, currentPrice);
        log.info("코인: {}, 손실률: {}", market.getCode(), lossRate);
        BigDecimal profitRate = calculateProfitRate(avgBuyPrice, currentPrice);
        log.info("코인: {}, 이익률: {}", market.getCode(), profitRate);
        
        log.info("사용자: {}, 마켓: {} - 보유수량: {}, 평균매수가: {}, 현재가: {}, 손실률: {}%, 이익률: {}%", 
            user.getUsername(), market, coinBalance, avgBuyPrice, currentPrice, 
            lossRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP),
            profitRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        
        // 1% 이상 손실인 경우 전량 매도
        if (lossRate.compareTo(LOSS_THRESHOLD) >= 0) {
            log.warn("사용자: {}, 마켓: {} - 1% 이상 손실 감지! 전량 매도 실행", user.getUsername(), market);
            executeFullSell(user, market, coinBalance, currentPrice, Strategy.LOSS_MANAGEMENT);
        }
        // 1.5% 이상 이익인 경우 전량 매도
        else if (profitRate.compareTo(PROFIT_THRESHOLD) >= 0) {
            log.info("사용자: {}, 마켓: {} - 1.5% 이상 이익 감지! 전량 매도 실행", user.getUsername(), market);
            executeFullSell(user, market, coinBalance, currentPrice, Strategy.PROFIT_TAKING);
        }
    }
    
    private BigDecimal getCurrentPrice(Market market) {
        List<TickerResponse> tickers = upbitQuotationClient.getTickers(List.of(market.getCode()));
        TickerResponse ticker = tickers.get(0);
        return new BigDecimal(ticker.tradePrice());
    }
    
    private BigDecimal calculateLossRate(BigDecimal avgBuyPrice, BigDecimal currentPrice) {
        // 손실률 = (평균매수가 - 현재가) / 평균매수가
        return avgBuyPrice.subtract(currentPrice).divide(avgBuyPrice, 4, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateProfitRate(BigDecimal avgBuyPrice, BigDecimal currentPrice) {
        // 이익률 = (현재가 - 평균매수가) / 평균매수가
        return currentPrice.subtract(avgBuyPrice).divide(avgBuyPrice, 4, RoundingMode.HALF_UP);
    }
    
    private void executeFullSell(User user, Market market, BigDecimal coinBalance, BigDecimal currentPrice, Strategy strategy) {
        try {
            // 시장가 매도 주문 생성
            String sellQuantityStr = coinBalance.toPlainString();
            OrderRequest orderRequest = OrderRequest.createSellOrder(market.getCode(), sellQuantityStr, currentPrice.toString());
            
            // 매도 주문 실행
            OrderResponse orderResponse = upbitExchangeClient.createOrder(user.getUsername(), orderRequest);
            
            // 주문 결과 저장
            tradeService.saveTrade(user, market.getCode(), strategy, orderResponse, currentPrice);
            
            String actionType = strategy == Strategy.LOSS_MANAGEMENT ? "손실 관리" : "이익 실현";
            log.info("사용자: {}, 마켓: {} - {} 전량 매도 주문 완료. 주문ID: {}, 수량: {}, 가격: {}", 
                user.getUsername(), market, actionType, orderResponse.uuid(), sellQuantityStr, currentPrice);
                
        } catch (Exception e) {
            log.error("사용자: {}, 마켓: {} - 전량 매도 주문 실패", user.getUsername(), market, e);
        }
    }
} 