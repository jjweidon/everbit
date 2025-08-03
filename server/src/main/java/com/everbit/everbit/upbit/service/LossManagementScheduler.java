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
    private static final String[] MARKETS = {"KRW-BTC", "KRW-ETH", "KRW-SOL", "KRW-DOGE", "KRW-USDT"}; // 거래할 마켓 목록

    @Transactional
    @Scheduled(cron = "0 */20 * * * *") // 20분마다 실행
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
        log.info("사용자: {} - 손실 및 이익 관리 처리 시작", user.getUsername());
        
        try {
            // 전체 계좌 잔고 조회
            List<AccountResponse> accounts = upbitExchangeClient.getAccounts(user.getUsername());
            
            for (String market : MARKETS) {
                try {
                    processMarketLossAndProfitManagement(user, market, accounts);
                } catch (Exception e) {
                    log.error("사용자: {}, 마켓: {} - 손실 및 이익 관리 처리 실패", user.getUsername(), market, e);
                }
            }
        } catch (Exception e) {
            log.error("사용자: {} - 계좌 잔고 조회 실패", user.getUsername(), e);
        }
    }
    
    private void processMarketLossAndProfitManagement(User user, String market, List<AccountResponse> accounts) {
        // 해당 마켓의 코인 잔고 찾기
        String coinCurrency = getCoinCurrency(market);
        AccountResponse coinAccount = findAccountByCurrency(accounts, coinCurrency);
        
        if (coinAccount == null || new BigDecimal(coinAccount.balance()).compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("사용자: {}, 마켓: {} - 보유 수량이 없음", user.getUsername(), market);
            return;
        }
        
        BigDecimal coinBalance = new BigDecimal(coinAccount.balance());
        BigDecimal avgBuyPrice = new BigDecimal(coinAccount.avgBuyPrice());
        
        if (avgBuyPrice.compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("사용자: {}, 마켓: {} - 매수 평균가가 0 이하", user.getUsername(), market);
            return;
        }
        
        // 현재 가격 조회
        BigDecimal currentPrice = getCurrentPrice(market);
        
        // 손실률 및 이익률 계산
        BigDecimal lossRate = calculateLossRate(avgBuyPrice, currentPrice);
        BigDecimal profitRate = calculateProfitRate(avgBuyPrice, currentPrice);
        
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
    
    private String getCoinCurrency(String market) {
        // KRW-BTC -> BTC, KRW-ETH -> ETH 등으로 변환
        return market.substring(4); // "KRW-" 제거
    }
    
    private AccountResponse findAccountByCurrency(List<AccountResponse> accounts, String currency) {
        return accounts.stream()
            .filter(account -> account.currency().equals(currency))
            .findFirst()
            .orElse(null);
    }
    
    private BigDecimal getCurrentPrice(String market) {
        List<TickerResponse> tickers = upbitQuotationClient.getTickers(List.of(market));
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
    
    private void executeFullSell(User user, String market, BigDecimal coinBalance, BigDecimal currentPrice, Strategy strategy) {
        try {
            // 시장가 매도 주문 생성
            String sellQuantityStr = coinBalance.toPlainString();
            OrderRequest orderRequest = OrderRequest.createSellOrder(market, sellQuantityStr, currentPrice.toString());
            
            // 매도 주문 실행
            OrderResponse orderResponse = upbitExchangeClient.createOrder(user.getUsername(), orderRequest);
            
            // 주문 결과 저장
            tradeService.saveTrade(user, market, strategy, orderResponse, currentPrice);
            
            String actionType = strategy == Strategy.LOSS_MANAGEMENT ? "손실 관리" : "이익 실현";
            log.info("사용자: {}, 마켓: {} - {} 전량 매도 주문 완료. 주문ID: {}, 수량: {}, 가격: {}", 
                user.getUsername(), market, actionType, orderResponse.uuid(), sellQuantityStr, currentPrice);
                
        } catch (Exception e) {
            log.error("사용자: {}, 마켓: {} - 전량 매도 주문 실패", user.getUsername(), market, e);
        }
    }
} 