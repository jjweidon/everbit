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
public class LossAndProfitManagementScheduler {
    private final UserService userService;
    private final UpbitExchangeClient upbitExchangeClient;
    private final UpbitQuotationClient upbitQuotationClient;
    private final TradeService tradeService;
    
    private static final BigDecimal LOSS_THRESHOLD = new BigDecimal("0.01"); // 1% 손실 임계값
    private static final BigDecimal PROFIT_THRESHOLD = new BigDecimal("0.018"); // 1.8% 이익 임계값

    private static final BigDecimal LOSS_SELL_RATIO = new BigDecimal("0.9"); // 손실 매도 비율
    private static final BigDecimal PROFIT_SELL_RATIO = new BigDecimal("0.5"); // 이익 매도 비율

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
                    AccountResponse account = accounts.stream()
                        .filter(a -> a.currency().equals(market.name()))
                        .findFirst()
                        .orElse(null);
                    if (account == null || new BigDecimal(account.balance()).compareTo(BigDecimal.ZERO) <= 0) continue;
        
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
        BigDecimal coinBalance = new BigDecimal(account.balance());
        BigDecimal avgBuyPrice = new BigDecimal(account.avgBuyPrice());
        
        if (avgBuyPrice.compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("사용자: {}, 마켓: {} - 매수 평균가가 0 이하", user.getUsername(), market);
            return;
        }
        
        // 현재 가격 조회
        BigDecimal currentPrice = getCurrentPrice(market);
        
        // 수익률 계산 (양수: 이익, 음수: 손실)
        BigDecimal profitRate = currentPrice.subtract(avgBuyPrice).divide(avgBuyPrice, 4, RoundingMode.HALF_UP);
        log.info("사용자: {}, 마켓: {} - 보유수량: {}, 평균매수가: {}, 현재가: {}, 수익률: {}%", 
            user.getUsername(), market, coinBalance, avgBuyPrice, currentPrice, 
            profitRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        
        // LOSS_THRESHOLD 이상 손실인 경우 LOSS_SELL_RATIO 비율로 매도
        if (profitRate.compareTo(LOSS_THRESHOLD.negate()) <= 0) {
            log.warn("사용자: {}, 마켓: {} - {}% 이상 손실 감지! 부분 매도 실행", user.getUsername(), market, LOSS_THRESHOLD.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
            executePartialSell(user, market, coinBalance, currentPrice, Strategy.LOSS_MANAGEMENT, LOSS_SELL_RATIO);
        }
        // PROFIT_THRESHOLD 이상 이익인 경우 PROFIT_SELL_RATIO 비율로 매도
        else if (profitRate.compareTo(PROFIT_THRESHOLD) >= 0) {
            log.info("사용자: {}, 마켓: {} - {}% 이상 이익 감지! 부분 매도 실행", user.getUsername(), market, PROFIT_THRESHOLD.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
            executePartialSell(user, market, coinBalance, currentPrice, Strategy.PROFIT_TAKING, PROFIT_SELL_RATIO);
        }
    }
    
    private BigDecimal getCurrentPrice(Market market) {
        List<TickerResponse> tickers = upbitQuotationClient.getTickers(List.of(market.getCode()));
        TickerResponse ticker = tickers.get(0);
        return new BigDecimal(ticker.tradePrice());
    }
    
    private void executePartialSell(User user, Market market, BigDecimal coinBalance, BigDecimal currentPrice, Strategy strategy, BigDecimal sellRatio) {
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
            
            // 매도 수량이 보유 수량보다 크면 전체 수량 매도
            if (sellQuantity.compareTo(coinBalance) > 0) {
                sellQuantity = coinBalance;
                log.info("사용자: {}, 마켓: {} - 매도 수량이 보유 수량보다 커서 전체 수량 매도: {}", 
                    user.getUsername(), market, sellQuantity);
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
} 