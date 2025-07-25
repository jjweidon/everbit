package com.everbit.everbit.upbit.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import com.everbit.everbit.upbit.dto.quotation.TickerResponse;
import com.everbit.everbit.upbit.dto.exchange.OrderChanceResponse;
import com.everbit.everbit.upbit.dto.exchange.OrderRequest;
import com.everbit.everbit.upbit.dto.exchange.OrderResponse;
import com.everbit.everbit.upbit.dto.trading.TradingSignal;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.service.UserService;
import com.everbit.everbit.trade.service.TradeService;
import com.everbit.everbit.trade.entity.enums.SignalType;

import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("prod")  // prod 프로필에서만 활성화
public class TradingScheduler {
    private final TradingSignalService tradingSignalService;
    private final UpbitExchangeClient upbitExchangeClient;
    private final UpbitQuotationClient upbitQuotationClient;
    private final UserService userService;
    private final TradeService tradeService;
    
    private static final String[] MARKETS = {"KRW-BTC", "KRW-ETH", "KRW-SOL"}; // 거래할 마켓 목록
    private static final BigDecimal FIXED_ORDER_AMOUNT = new BigDecimal("6000"); // 고정 주문 금액 6000원
    
    @Transactional
    @Scheduled(fixedRate = 180000) // 3분마다 실행
    public void checkTradingSignals() {
        List<User> activeUsers = userService.findUsersWithActiveBots();
        
        for (User user : activeUsers) {
            for (String market : MARKETS) {
                try {
                    log.info("사용자: {}, 마켓: {} - 트레이딩 시그널 확인 중", user.getUsername(), market);
                    TradingSignal signal = tradingSignalService.calculateSignals(market);
                    processSignal(signal, user);
                } catch (Exception e) {
                    log.error("사용자: {}, 마켓: {} - 트레이딩 시그널 처리 실패", user.getUsername(), market, e);
                }
            }
        }
    }
    
    @Transactional
    private void processSignal(TradingSignal signal, User user) {
        String market = signal.market();
        
        // 매수 주문 로직
        if (signal.isBuySignal()) {
            log.info("마켓: {} - 매수 시그널 감지됨", market);
            try {
                // 1. 계좌 잔고 확인
                OrderChanceResponse orderChance = upbitExchangeClient.getOrderChance(user.getUsername(), market);
                BigDecimal availableBalance = new BigDecimal(orderChance.bidAccount().balance());
                BigDecimal minOrderKRW = new BigDecimal(orderChance.market().bid().minTotal())
                    .multiply(BigDecimal.ONE.add(new BigDecimal(orderChance.bidFee())));
                log.info("마켓: {} - 계좌 잔고: {}", market, availableBalance);
                
                // 2. 주문 가능 수량 계산 (고정 금액 6000원 사용)
                List<TickerResponse> tickers = upbitQuotationClient.getTickers(List.of(market));
                TickerResponse ticker = tickers.get(0);
                BigDecimal currentPrice = new BigDecimal(ticker.tradePrice());
                BigDecimal orderBalance = FIXED_ORDER_AMOUNT.max(minOrderKRW);
                log.info("마켓: {} - 매수 금액: {}", market, orderBalance);
                BigDecimal orderAmount = orderBalance.divide(currentPrice, 8, RoundingMode.HALF_UP);

                // 3. 주문 실행
                if (orderBalance.compareTo(minOrderKRW) < 0) {
                    log.info("마켓: {} - 매수 금액({})이 최소 주문 금액({})보다 작아 매수 건너뜀", market, orderBalance, minOrderKRW);
                    return;
                }

                if (availableBalance.compareTo(orderBalance) < 0) {
                    log.info("마켓: {} - 계좌 잔고({})가 고정 매수 금액({})보다 작아 매수 건너뜀", market, availableBalance, orderBalance);
                    return;
                }
                
                OrderRequest orderRequest = OrderRequest.createBuyOrder(market, orderAmount.toString(), currentPrice.toString());
                OrderResponse orderResponse = upbitExchangeClient.createOrder(user.getUsername(), orderRequest);

                // 4. 주문 결과 저장
                SignalType signalType = determineSignalType(signal);
                tradeService.saveTrade(user, market, orderResponse, currentPrice, signalType);
                log.info("마켓: {} - 매수 주문 실행 및 저장 완료. 주문 정보: {}, 시그널: {}", 
                    market, orderResponse, signalType.getDescription());
            } catch (Exception e) {
                log.error("사용자: {}, 마켓: {} - 매수 시그널 처리 실패", user.getUsername(), market, e);
            }
        }
        
        // 매도 주문 로직
        if (signal.isSellSignal()) {
            log.info("마켓: {} - 매도 시그널 감지됨", market);
            try {
                // 1. 보유 수량 확인
                OrderChanceResponse orderChance = upbitExchangeClient.getOrderChance(user.getUsername(), market);
                BigDecimal availableAmount = new BigDecimal(orderChance.askAccount().balance());
                log.info("마켓: {} - 보유 수량: {}", market, availableAmount);
                
                if (availableAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    log.info("마켓: {} - 보유 수량이 없어 매도 건너뜀", market);
                    return;
                }

                // 2. 주문 실행
                List<TickerResponse> tickers = upbitQuotationClient.getTickers(List.of(market));
                TickerResponse ticker = tickers.get(0);
                BigDecimal currentPrice = new BigDecimal(ticker.tradePrice());
                log.info("마켓: {} - 현재 가격: {}", market, currentPrice);

                // 고정 금액(6000원)에 맞는 매도 수량 계산
                BigDecimal sellAmount = FIXED_ORDER_AMOUNT.divide(currentPrice, 8, RoundingMode.DOWN);
                log.info("마켓: {} - 매도 수량: {}", market, sellAmount);

                if (sellAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    log.info("마켓: {} - 매도 수량이 0 이하여서 매도 건너뜀", market);
                    return;
                }

                // 보유 수량보다 매도 수량이 많으면 보유 수량만큼만 매도
                if (sellAmount.compareTo(availableAmount) > 0) {
                    sellAmount = availableAmount;
                    log.info("마켓: {} - 매도 수량을 보유 수량으로 조정: {}", market, sellAmount);
                }

                // 매도 주문 금액 계산 및 최소 주문 금액 체크
                BigDecimal orderValue = sellAmount.multiply(currentPrice);
                BigDecimal minOrderValue = new BigDecimal(orderChance.market().ask().minTotal());
                
                if (orderValue.compareTo(minOrderValue) < 0) {
                    log.info("마켓: {} - 매도 주문 금액({})이 최소 주문 금액({})보다 작아 매도 건너뜀", 
                        market, orderValue, minOrderValue);
                    return;
                }

                OrderRequest orderRequest = OrderRequest.createSellOrder(market, sellAmount.toString(), currentPrice.toString());
                OrderResponse orderResponse = upbitExchangeClient.createOrder(user.getUsername(), orderRequest);

                // 3. 주문 결과 저장
                SignalType signalType = determineSignalType(signal);
                tradeService.saveTrade(user, market, orderResponse, currentPrice, signalType);
                log.info("마켓: {} - 매도 주문 실행 및 저장 완료. 주문 수량: {}, 주문 정보: {}, 시그널: {}", 
                    market, sellAmount, orderResponse, signalType.getDescription());
            } catch (Exception e) {
                log.error("사용자: {}, 마켓: {} - 매도 시그널 처리 실패", user.getUsername(), market, e);
            }
        }
    }

    private SignalType determineSignalType(TradingSignal signal) {
        if (signal.goldenCross()) return SignalType.GOLDEN_CROSS;
        if (signal.macdBuySignal()) return SignalType.MACD_BUY;
        if (signal.rsiOversold()) return SignalType.RSI_OVERSOLD;
        if (signal.bbOverSold()) return SignalType.BB_OVERSOLD;
        if (signal.deadCross()) return SignalType.DEAD_CROSS;
        if (signal.macdSellSignal()) return SignalType.MACD_SELL;
        if (signal.rsiOverbought()) return SignalType.RSI_OVERBOUGHT;
        if (signal.bbOverBought()) return SignalType.BB_OVERBOUGHT;
        return SignalType.UNKNOWN;
    }
} 