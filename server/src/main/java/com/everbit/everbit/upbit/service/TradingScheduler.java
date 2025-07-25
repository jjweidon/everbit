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
import com.everbit.everbit.trade.entity.enums.Strategy;

import com.everbit.everbit.global.config.util.RedisUtils;

import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

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
    private final RedisUtils redisUtils;
    
    private static final String[] MARKETS = {"KRW-BTC", "KRW-ETH", "KRW-SOL"}; // 거래할 마켓 목록
    private static final BigDecimal BUY_AMOUNT_RATIO = new BigDecimal("0.25"); // 잔고의 25%만 주문
    private static final BigDecimal SELL_AMOUNT_RATIO = new BigDecimal("0.50"); // 보유 수량의 50%만 매도
    private static final String LAST_EXECUTION_KEY_PREFIX = "trading:last_execution:";
    private static final String RATE_LIMIT_KEY_PREFIX = "trading:rate_limit:";
    private static final long RATE_LIMIT_DURATION = 60; // 60초 제한
    
    // Redis key 생성
    private String createRedisKey(String userId, String market, Strategy strategy) {
        return LAST_EXECUTION_KEY_PREFIX + userId + ":" + market + ":" + strategy.name();
    }
    
    // 마지막 실행 시간 조회
    private long getLastExecutionTime(String userId, String market, Strategy strategy) {
        String key = createRedisKey(userId, market, strategy);
        String value = redisUtils.getData(key);
        return value != null ? Long.parseLong(value) : 0L;
    }
    
    // 마지막 실행 시간 업데이트
    private void updateLastExecutionTime(String userId, String market, Strategy strategy, long timestamp) {
        String key = createRedisKey(userId, market, strategy);
        redisUtils.setData(key, String.valueOf(timestamp), strategy.getIntervalSeconds() * 3L);
    }
    
    // 요청 제한 확인
    private boolean isRateLimited(String market) {
        String key = RATE_LIMIT_KEY_PREFIX + market;
        String value = redisUtils.getData(key);
        return value != null;
    }
    
    // 요청 제한 설정
    private void setRateLimit(String market) {
        String key = RATE_LIMIT_KEY_PREFIX + market;
        redisUtils.setData(key, "1", RATE_LIMIT_DURATION);
    }
    
    @Transactional
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void checkTradingSignals() {
        List<User> activeUsers = userService.findUsersWithActiveBots();
        Instant now = Instant.now();
        
        for (User user : activeUsers) {
            String userId = user.getId();
            
            for (String market : MARKETS) {
                try {
                    log.debug("사용자: {}, 마켓: {} - 전략 실행 시간 확인", user.getUsername(), market);
                    
                    // 실행할 전략들을 먼저 확인
                    List<Strategy> strategiesToExecute = new ArrayList<>();
                    for (Strategy strategy : Strategy.values()) {
                        long lastExecution = getLastExecutionTime(userId, market, strategy);
                        long currentTime = now.toEpochMilli();
                        
                        // 전략의 실행 주기가 되었는지 확인
                        if (currentTime - lastExecution >= strategy.getIntervalMillis()) {
                            strategiesToExecute.add(strategy);
                        }
                    }
                    
                    // 실행할 전략이 있는 경우에만 캔들 데이터를 가져옴
                    if (!strategiesToExecute.isEmpty()) {
                        // 요청 제한 확인
                        if (isRateLimited(market)) {
                            log.info("사용자: {}, 마켓: {} - 요청 제한으로 인해 건너뜀", user.getUsername(), market);
                            continue;
                        }
                        
                        log.info("사용자: {}, 마켓: {} - {}개 전략 실행 예정", 
                            user.getUsername(), market, strategiesToExecute.size());
                        
                        TradingSignal signal = tradingSignalService.calculateSignals(market);
                        
                        // 요청 제한 설정
                        setRateLimit(market);
                        
                        // 각 전략별로 처리
                        for (Strategy strategy : strategiesToExecute) {
                            log.info("사용자: {}, 마켓: {}, 전략: {} - 시그널 체크 실행", 
                                user.getUsername(), market, strategy.getName());
                            
                            // 현재 전략에 맞는 시그널인 경우에만 처리
                            Strategy determinedStrategy = signal.determineStrategy();
                            
                            if (determinedStrategy == null) {
                                log.debug("사용자: {}, 마켓: {}, 전략: {} - 시그널 없음", 
                                    user.getUsername(), market, strategy.getName());
                            } else {
                                log.debug("사용자: {}, 마켓: {}, 전략: {} - 결정된 전략: {}, 매수시그널: {}, 매도시그널: {}", 
                                    user.getUsername(), market, strategy.getName(), determinedStrategy.getName(),
                                    signal.isBuySignal(), signal.isSellSignal());
                                
                                if (determinedStrategy == strategy) {
                                    processSignal(signal, user);
                                } else {
                                    log.debug("사용자: {}, 마켓: {}, 전략: {} - 시그널 불일치로 건너뜀", 
                                        user.getUsername(), market, strategy.getName());
                                }
                            }
                            
                            // 실행 시간 업데이트
                            updateLastExecutionTime(userId, market, strategy, now.toEpochMilli());
                        }
                    }
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
                
                // 2. 주문 가능 수량 계산 (잔고의 25%만 사용)
                List<TickerResponse> tickers = upbitQuotationClient.getTickers(List.of(market));
                TickerResponse ticker = tickers.get(0);
                BigDecimal currentPrice = new BigDecimal(ticker.tradePrice());
                BigDecimal orderBalance = availableBalance.multiply(BUY_AMOUNT_RATIO).max(minOrderKRW);
                log.info("마켓: {} - 매수 금액: {}", market, orderBalance);
                BigDecimal orderAmount = orderBalance.divide(currentPrice, 8, RoundingMode.HALF_UP);

                // 3. 주문 실행
                if (orderBalance.compareTo(minOrderKRW) < 0) {
                    log.info("마켓: {} - 매수 금액({})이 최소 주문 금액({})보다 작아 매수 건너뜀", market, orderBalance, minOrderKRW);
                    return;
                }

                if (availableBalance.compareTo(orderBalance) < 0) {
                    log.info("마켓: {} - 계좌 잔고({})가 매수 금액({})보다 작아 매수 건너뜀", market, availableBalance, orderBalance);
                    return;
                }
                
                OrderRequest orderRequest = OrderRequest.createBuyOrder(market, orderAmount.toString(), currentPrice.toString());
                OrderResponse orderResponse = upbitExchangeClient.createOrder(user.getUsername(), orderRequest);

                // 주문 결과 저장
                Strategy strategy = signal.determineStrategy();
                tradeService.saveTrade(user, market, orderResponse, currentPrice, strategy);
                log.info("마켓: {} - 매수 주문 실행 및 저장 완료. 주문 정보: {}, 전략: {}", 
                    market, orderResponse, strategy.getName());
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

                // 보유 수량의 50%만 매도 (소수점 8자리까지만 사용)
                BigDecimal sellAmount = availableAmount.multiply(SELL_AMOUNT_RATIO)
                    .setScale(8, RoundingMode.DOWN);
                log.info("마켓: {} - 매도 수량: {}", market, sellAmount);

                if (sellAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    log.info("마켓: {} - 매도 수량이 0 이하여서 매도 건너뜀", market);
                    return;
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

                // 주문 결과 저장
                Strategy strategy = signal.determineStrategy();
                tradeService.saveTrade(user, market, orderResponse, currentPrice, strategy);
                log.info("마켓: {} - 매도 주문 실행 및 저장 완료. 주문 수량: {}, 주문 정보: {}, 전략: {}", 
                    market, sellAmount, orderResponse, strategy.getName());
            } catch (Exception e) {
                log.error("사용자: {}, 마켓: {} - 매도 시그널 처리 실패", user.getUsername(), market, e);
            }
        }
    }
} 