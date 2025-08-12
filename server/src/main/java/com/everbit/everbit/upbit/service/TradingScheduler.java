package com.everbit.everbit.upbit.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.annotation.Order;

import com.everbit.everbit.upbit.dto.quotation.TickerResponse;
import com.everbit.everbit.upbit.dto.exchange.OrderChanceResponse;
import com.everbit.everbit.upbit.dto.exchange.OrderRequest;
import com.everbit.everbit.upbit.dto.exchange.OrderResponse;
import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.trade.entity.enums.Strategy;
import com.everbit.everbit.upbit.dto.trading.TradingSignal;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.service.UserService;
import com.everbit.everbit.trade.service.TradeService;
import com.everbit.everbit.user.entity.BotSetting;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("prod")
@Order(3)
public class TradingScheduler {
    private final TradingSignalService tradingSignalService;
    private final StrategyService strategyService;
    private final UpbitExchangeClient upbitExchangeClient;
    private final UpbitQuotationClient upbitQuotationClient;
    private final UserService userService;
    private final TradeService tradeService;
    
    @Transactional
    @Scheduled(cron = "2 */3 * * * *")
    public void checkTradingSignals() {
        // 1. 모든 마켓에 대한 시그널을 먼저 계산
        Map<Market, TradingSignal> marketSignals = new HashMap<>();
        for (Market market : Market.values()) {
            try {
                log.info("마켓: {} - 기본 시그널 계산 중", market.getCode());
                TradingSignal signal = tradingSignalService.calculateBasicSignals(market.getCode());
                marketSignals.put(market, signal);
            } catch (Exception e) {
                log.error("마켓: {} - 기본 시그널 계산 실패", market.getCode(), e);
            }
        }
        
        // 2. 활성 사용자별로 시그널 처리
        List<User> activeUsers = userService.findUsersWithActiveBots();
        for (User user : activeUsers) {
            for (Market market : user.getBotSetting().getMarketList()) {
                TradingSignal signal = marketSignals.get(market);
                if (signal != null) {
                    try {
                        log.info("사용자: {}, 마켓: {} - 트레이딩 시그널 처리 중", user.getUsername(), market);
                        processSignal(signal, user);
                    } catch (Exception e) {
                        log.error("사용자: {}, 마켓: {} - 트레이딩 시그널 처리 실패", user.getUsername(), market, e);
                    }
                }
            }
        }
    }
    
    @Transactional
    private void processSignal(TradingSignal signal, User user) {
        String marketCode = signal.market();
        BotSetting botSetting = user.getBotSetting();
        Strategy buyStrategy = botSetting.getBuyStrategy();
        Strategy sellStrategy = botSetting.getSellStrategy();
        
        // 지표값 상세 로깅
        log.info("사용자: {}, 마켓: {} - 지표값 [\n" +
                "  현재가: {}\n" +
                "  BB하단: {}\n" +
                "  BB중간: {}\n" +
                "  BB상단: {}\n" +
                "  RSI: {}\n" +
                "  MACD: {}\n" +
                "  MACD시그널: {}\n" +
                "  MACD히스토그램: {}\n" +
                "  DROP_N_FLIP: {}\n" +
                "  POP_N_FLIP: {}\n" +
                "]", 
                user.getUsername(), marketCode,
                signal.currentPrice().doubleValue(),
                signal.bbLowerBand().doubleValue(),
                signal.bbMiddleBand().doubleValue(),
                signal.bbUpperBand().doubleValue(),
                signal.rsiValue().doubleValue(),
                signal.macdValue().doubleValue(),
                signal.macdSignalValue().doubleValue(),
                signal.macdHistogram().doubleValue(),
                signal.dropNFlipStrength(),
                signal.popNFlipStrength());
        
        // 전략별 매수/매도 시그널 결정
        boolean buySignal = strategyService.determineBuySignal(signal, buyStrategy);
        boolean sellSignal = strategyService.determineSellSignal(signal, sellStrategy);
        
         // 매수/매도 시그널 강도 계산
         double buySignalStrength = strategyService.calculateSignalStrength(signal, buyStrategy);
         double sellSignalStrength = strategyService.calculateSignalStrength(signal, sellStrategy);
         
         // 개별 지표 시그널 상태 로깅
         log.info("사용자: {}, 마켓: {} - 개별 지표 시그널 [\n" +
                "  BB: 매수={}, 매도={},\n" +
                "  RSI: 매수={}, 매도={},\n" +
                "  MACD: 매수={}, 매도={},\n" +
                "  DROP_N_FLIP={}, POP_N_FLIP={},\n" +
                "  최종 시그널: 매수={}, 매도={}\n" +
                "]", 
        user.getUsername(), marketCode, 
        signal.bbBuySignal(), signal.bbSellSignal(),
        signal.rsiBuySignal(), signal.rsiSellSignal(),
        signal.macdBuySignal(), signal.macdSellSignal(),
        signal.dropNFlipBuySignal(), signal.popNFlipSellSignal(),
        buySignal, sellSignal);
        
        // 시그널 상태 로그 출력
        if (!buySignal && !sellSignal) {
            log.info("사용자: {}, 마켓: {} - 매수전략: {}, 매도전략: {}, 시그널 없음 (매수강도: {}, 매도강도: {})", 
                    user.getUsername(), marketCode, buyStrategy.getValue(), sellStrategy.getValue(), 
                    String.format("%.2f", buySignalStrength), String.format("%.2f", sellSignalStrength));
        }
        
        // 매수 주문 로직
        if (buySignal) {
            log.info("마켓: {} - 매수 시그널 감지됨 (전략: {}, 시그널 강도: {})", marketCode, buyStrategy, String.format("%.2f", buySignalStrength));
            try {
                // 매수용 최소/최대 주문금액 가져오기
                BigDecimal buyBaseOrderAmount = BigDecimal.valueOf(botSetting.getBuyBaseOrderAmount());
                BigDecimal buyMaxOrderAmount = BigDecimal.valueOf(botSetting.getBuyMaxOrderAmount());
                
                // 1. 계좌 잔고 확인
                OrderChanceResponse orderChance = upbitExchangeClient.getOrderChance(user.getUsername(), marketCode);
                BigDecimal availableBalance = new BigDecimal(orderChance.bidAccount().balance());
                log.info("마켓: {} - 계좌 잔고: {}", marketCode, availableBalance);

                if (availableBalance.compareTo(buyBaseOrderAmount) < 0) {
                    log.info("마켓: {} - 계좌 잔고가 최소 주문 금액({}) 이하이므로 매수 건너뜀", marketCode, buyBaseOrderAmount);
                    return;
                }
                
                // 2. 주문 금액 계산
                BigDecimal currentPrice = getCurrentPrice(marketCode);
                BigDecimal buyAmount = strategyService.calculateOrderAmountBySignalStrength(buySignalStrength, buyBaseOrderAmount, buyMaxOrderAmount);
                buyAmount = buyAmount.min(availableBalance);

                if (availableBalance.compareTo(buyAmount) < 0) {
                    log.info("마켓: {} - 계좌 잔고({})가 매수 금액({})보다 작아 매수 건너뜀", marketCode, availableBalance, buyAmount);
                    return;
                }

                // 3. 주문 수량 계산 및 주문 실행
                log.info("마켓: {} - 매수 금액: {}", marketCode, buyAmount);
                BigDecimal buyQuantity = buyAmount.divide(currentPrice, 8, RoundingMode.HALF_UP);
                
                // BigDecimal을 일반 표기법으로 변환하여 주문 수량 문자열 생성
                String buyQuantityStr = buyQuantity.toPlainString();
                OrderRequest orderRequest = OrderRequest.createBuyOrder(marketCode, buyQuantityStr, currentPrice.toString());
                OrderResponse orderResponse = upbitExchangeClient.createOrder(user.getUsername(), orderRequest);

                // 4. 주문 결과 저장
                tradeService.saveTrade(user, marketCode, buyStrategy, orderResponse, currentPrice);
                log.info("마켓: {} - 매수 주문 실행 및 저장 완료 (전략: {}). 주문 정보: [상태: {}, 수량: {}, 가격: {}, 주문금액: {}]", 
                    marketCode, buyStrategy.getValue(),
                    orderResponse.state(), orderResponse.volume(), 
                    orderResponse.price(), orderResponse.executedFunds());
            } catch (Exception e) {
                log.error("사용자: {}, 마켓: {} - 매수 시그널 처리 실패", user.getUsername(), marketCode, e);
            }
        }
        
        // 매도 주문 로직
        if (sellSignal) {
            log.info("마켓: {} - 매도 시그널 감지됨 (전략: {}, 시그널 강도: {})", marketCode, sellStrategy, String.format("%.2f", sellSignalStrength));
            try {
                // 매도용 최소/최대 주문금액 가져오기
                BigDecimal sellBaseOrderAmount = BigDecimal.valueOf(botSetting.getSellBaseOrderAmount());
                BigDecimal sellMaxOrderAmount = BigDecimal.valueOf(botSetting.getSellMaxOrderAmount());
                
                // 1. 보유 수량 확인
                OrderChanceResponse orderChance = upbitExchangeClient.getOrderChance(user.getUsername(), marketCode);
                BigDecimal availableQuantity = new BigDecimal(orderChance.askAccount().balance());
                log.info("마켓: {} - 보유 수량: {}", marketCode, availableQuantity);
                
                if (availableQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                    log.info("마켓: {} - 보유 수량이 없어 매도 건너뜀", marketCode);
                    return;
                }

                // 2. 주문 금액 계산
                BigDecimal currentPrice = getCurrentPrice(marketCode);
                BigDecimal sellAmount = strategyService.calculateOrderAmountBySignalStrength(sellSignalStrength, sellBaseOrderAmount, sellMaxOrderAmount);
                sellAmount = sellAmount.min(availableQuantity.multiply(currentPrice));

                // 3. 주문 수량 계산 및 주문 실행
                BigDecimal sellQuantity = sellAmount.divide(currentPrice, 8, RoundingMode.DOWN);

                // 4. 매도 후 남을 수량 계산
                BigDecimal remainingQuantity = availableQuantity.subtract(sellQuantity);
                BigDecimal remainingAmount = remainingQuantity.multiply(currentPrice);
                if (remainingAmount.compareTo(sellBaseOrderAmount) <= 0) {
                    sellQuantity = availableQuantity;
                    log.info("마켓: {} - 남은 주문 가능 금액 {}이 SELL_BASE_ORDER_AMOUNT({})이하이므로 전체 보유 수량 매도: {}", 
                        marketCode, remainingAmount, sellBaseOrderAmount, sellQuantity.multiply(currentPrice));
                }

                // 5. 매도 주문 금액 계산 및 주문 실행
                sellAmount = sellQuantity.multiply(currentPrice);
                
                // BigDecimal을 일반 표기법으로 변환하여 주문 수량 문자열 생성
                String sellQuantityStr = sellQuantity.toPlainString();
                OrderRequest orderRequest = OrderRequest.createSellOrder(marketCode, sellQuantityStr, currentPrice.toString());
                OrderResponse orderResponse = upbitExchangeClient.createOrder(user.getUsername(), orderRequest);

                // 6. 주문 결과 저장
                tradeService.saveTrade(user, marketCode, sellStrategy, orderResponse, currentPrice);
                log.info("마켓: {} - 매도 주문 실행 및 저장 완료 (전략: {}). 주문 정보: [상태: {}, 수량: {}, 가격: {}, 주문금액: {}]", 
                    marketCode, sellStrategy.getValue(),
                    orderResponse.state(), orderResponse.volume(), 
                    orderResponse.price(), orderResponse.executedFunds());
            } catch (Exception e) {
                log.error("사용자: {}, 마켓: {} - 매도 시그널 처리 실패", user.getUsername(), marketCode, e);
            }
        }
    }

    private BigDecimal getCurrentPrice(String market) {
        List<TickerResponse> tickers = upbitQuotationClient.getTickers(List.of(market));
        TickerResponse ticker = tickers.get(0);
        return new BigDecimal(ticker.tradePrice()).setScale(8, RoundingMode.HALF_UP);
    }
} 