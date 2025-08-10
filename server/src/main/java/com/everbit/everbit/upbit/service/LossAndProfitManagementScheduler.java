package com.everbit.everbit.upbit.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.annotation.Order;

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
@Order(2)  // 2순위
public class LossAndProfitManagementScheduler {
    private final UserService userService;
    private final UpbitExchangeClient upbitExchangeClient;
    private final UpbitQuotationClient upbitQuotationClient;
    private final TradeService tradeService;

    @Transactional
    @Scheduled(cron = "0 */3 * * * *", initialDelay = 2000) // 3분마다 실행
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
        
        // BotSetting에서 손실/이익 임계값과 비율 가져오기
        BigDecimal lossThreshold = user.getBotSetting().getLossThreshold();
        BigDecimal profitThreshold = user.getBotSetting().getProfitThreshold();
        BigDecimal lossSellRatio = user.getBotSetting().getLossSellRatio();
        BigDecimal profitSellRatio = user.getBotSetting().getProfitSellRatio();
        
        // 손실 임계값 이상 손실인 경우 손실 매도 비율로 매도
        if (profitRate.compareTo(lossThreshold.negate()) <= 0) {
            log.warn("사용자: {}, 마켓: {} - {}% 이상 손실 감지! 부분 매도 실행", user.getUsername(), market, lossThreshold.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
            executePartialSell(user, market, coinBalance, currentPrice, Strategy.LOSS_MANAGEMENT, lossSellRatio);
        }
        // 이익 임계값 이상 이익인 경우 이익 매도 비율로 매도
        else if (profitRate.compareTo(profitThreshold) >= 0) {
            log.info("사용자: {}, 마켓: {} - {}% 이상 이익 감지! 부분 매도 실행", user.getUsername(), market, profitThreshold.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
            executePartialSell(user, market, coinBalance, currentPrice, Strategy.PROFIT_TAKING, profitSellRatio);
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
} 