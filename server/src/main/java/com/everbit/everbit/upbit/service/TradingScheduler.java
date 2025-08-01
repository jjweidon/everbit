package com.everbit.everbit.upbit.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Transactional
    @Scheduled(cron = "0 */5 * * * *")
    public void checkTradingSignals() {
        List<User> activeUsers = userService.findUsersWithActiveBots();
        
        for (User user : activeUsers) {
            for (Market market : user.getBotSetting().getMarketList()) {
                try {
                    log.info("사용자: {}, 마켓: {} - 트레이딩 시그널 확인 중", user.getUsername(), market);
                    TradingSignal signal = tradingSignalService.calculateSignals(market.getCode(), user);
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
        BigDecimal baseOrderAmount = new BigDecimal(user.getBotSetting().getBaseOrderAmount());
        BigDecimal maxOrderAmount = new BigDecimal(user.getBotSetting().getMaxOrderAmount());
        Strategy strategy = user.getBotSetting().getStrategy();
        
        // 사용자가 선택한 전략에 따라 매수 시그널 결정
        boolean buySignal = determineBuySignal(signal, strategy);
        boolean sellSignal = determineSellSignal(signal, strategy);
        
        // 매수 주문 로직
        if (buySignal) {
            log.info("마켓: {} - 매수 시그널 감지됨 (전략: {})", market, strategy);
            try {
                // 1. 계좌 잔고 확인
                OrderChanceResponse orderChance = upbitExchangeClient.getOrderChance(user.getUsername(), market);
                BigDecimal availableBalance = new BigDecimal(orderChance.bidAccount().balance());
                log.info("마켓: {} - 계좌 잔고: {}", market, availableBalance);

                if (availableBalance.compareTo(baseOrderAmount) < 0) {
                    log.info("마켓: {} - 계좌 잔고가 최소 주문 금액({}) 이하이므로 매수 건너뜀", market, baseOrderAmount);
                    return;
                }
                
                // 2. 주문 금액 계산
                BigDecimal currentPrice = getCurrentPrice(market);
                BigDecimal buyAmount = tradingSignalService.calculateOrderAmountBySignalStrength(signal, strategy, baseOrderAmount, maxOrderAmount);
                buyAmount = buyAmount.min(availableBalance);

                if (availableBalance.compareTo(buyAmount) < 0) {
                    log.info("마켓: {} - 계좌 잔고({})가 매수 금액({})보다 작아 매수 건너뜀", market, availableBalance, buyAmount);
                    return;
                }

                // 3. 주문 수량 계산 및 주문 실행
                log.info("마켓: {} - 매수 금액: {}", market, buyAmount);
                BigDecimal buyQuantity = buyAmount.divide(currentPrice, 8, RoundingMode.HALF_UP);
                
                // BigDecimal을 일반 표기법으로 변환하여 주문 수량 문자열 생성
                String buyQuantityStr = buyQuantity.toPlainString();
                OrderRequest orderRequest = OrderRequest.createBuyOrder(market, buyQuantityStr, currentPrice.toString());
                OrderResponse orderResponse = upbitExchangeClient.createOrder(user.getUsername(), orderRequest);

                // 4. 주문 결과 저장
                SignalType signalType = determineSignalType(signal);
                tradeService.saveTrade(user, market, strategy, orderResponse, currentPrice);
                log.info("마켓: {} - 매수 주문 실행 및 저장 완료 (전략: {}). 주문 정보: {}, 시그널: {}", 
                    market, strategy.getValue(), orderResponse, signalType.getDescription());
            } catch (Exception e) {
                log.error("사용자: {}, 마켓: {} - 매수 시그널 처리 실패", user.getUsername(), market, e);
            }
        }
        
        // 매도 주문 로직
        if (sellSignal) {
            log.info("마켓: {} - 매도 시그널 감지됨 (전략: {})", market, strategy);
            try {
                // 1. 보유 수량 확인
                OrderChanceResponse orderChance = upbitExchangeClient.getOrderChance(user.getUsername(), market);
                BigDecimal availableQuantity = new BigDecimal(orderChance.askAccount().balance());
                log.info("마켓: {} - 보유 수량: {}", market, availableQuantity);
                
                if (availableQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                    log.info("마켓: {} - 보유 수량이 없어 매도 건너뜀", market);
                    return;
                }

                // 2. 주문 금액 계산
                BigDecimal currentPrice = getCurrentPrice(market);
                BigDecimal sellAmount = tradingSignalService.calculateOrderAmountBySignalStrength(signal, strategy, baseOrderAmount, maxOrderAmount);
                sellAmount = sellAmount.min(availableQuantity.multiply(currentPrice));

                // 3. 주문 수량 계산 및 주문 실행
                BigDecimal sellQuantity = sellAmount.divide(currentPrice, 8, RoundingMode.DOWN);

                // 4. 매도 후 남을 수량 계산
                BigDecimal remainingQuantity = availableQuantity.subtract(sellQuantity);
                BigDecimal remainingAmount = remainingQuantity.multiply(currentPrice);
                if (remainingAmount.compareTo(baseOrderAmount) <= 0) {
                    sellQuantity = availableQuantity;
                    log.info("마켓: {} - 남은 주문 가능 금액 {}이 BASE_ORDER_AMOUNT({})이하이므로 전체 보유 수량 매도: {}", 
                        market, remainingAmount, baseOrderAmount, sellQuantity.multiply(currentPrice));
                }

                // 5. 매도 주문 금액 계산 및 주문 실행
                sellAmount = sellQuantity.multiply(currentPrice);
                
                // BigDecimal을 일반 표기법으로 변환하여 주문 수량 문자열 생성
                String sellQuantityStr = sellQuantity.toPlainString();
                OrderRequest orderRequest = OrderRequest.createSellOrder(market, sellQuantityStr, currentPrice.toString());
                OrderResponse orderResponse = upbitExchangeClient.createOrder(user.getUsername(), orderRequest);

                // 6. 주문 결과 저장
                SignalType signalType = determineSignalType(signal);
                tradeService.saveTrade(user, market, strategy, orderResponse, currentPrice);
                log.info("마켓: {} - 매도 주문 실행 및 저장 완료 (전략: {}). 주문 수량: {}, 주문 정보: {}, 시그널: {}", 
                    market, strategy.getValue(), sellQuantity, orderResponse, signalType.getDescription());
            } catch (Exception e) {
                log.error("사용자: {}, 마켓: {} - 매도 시그널 처리 실패", user.getUsername(), market, e);
            }
        }
    }

    /**
     * 사용자가 선택한 전략에 따라 매수 시그널을 결정합니다.
     */
    private boolean determineBuySignal(TradingSignal signal, Strategy strategy) {
        switch (strategy) {
            case BOLLINGER_MEAN_REVERSION:
                return signal.isBollingerMeanReversionBuySignal();
            case BB_MOMENTUM:
                return signal.isBbMomentumBuySignal();
            case EMA_MOMENTUM:
                return signal.isEmaMomentumBuySignal();
            case ENSEMBLE:
                return signal.isEnsembleBuySignal();
            case ENHANCED_ENSEMBLE:
                return signal.isEnhancedEnsembleBuySignal();
            default:
                return signal.isBollingerMeanReversionBuySignal();
        }
    }
    
    /**
     * 사용자가 선택한 전략에 따라 매도 시그널을 결정합니다.
     */
    private boolean determineSellSignal(TradingSignal signal, Strategy strategy) {
        switch (strategy) {
            case BOLLINGER_MEAN_REVERSION:
                return signal.isBollingerMeanReversionSellSignal();
            case BB_MOMENTUM:
                return signal.isBbMomentumSellSignal();
            case EMA_MOMENTUM:
                return signal.isEmaMomentumSellSignal();
            case ENSEMBLE:
                return signal.isEnsembleSellSignal();
            case ENHANCED_ENSEMBLE:
                return signal.isEnhancedEnsembleSellSignal();
            default:
                return signal.isBollingerMeanReversionSellSignal();
        }
    }

    private SignalType determineSignalType(TradingSignal signal) {
        if (signal.isBollingerMeanReversionBuySignal()) return SignalType.BOLLINGER_MEAN_REVERSION_BUY;
        if (signal.isBollingerMeanReversionSellSignal()) return SignalType.BOLLINGER_MEAN_REVERSION_SELL;
        if (signal.isBbMomentumBuySignal()) return SignalType.BB_MOMENTUM_BUY;
        if (signal.isBbMomentumSellSignal()) return SignalType.BB_MOMENTUM_SELL;
        if (signal.isEmaMomentumBuySignal()) return SignalType.EMA_MOMENTUM_BUY;
        if (signal.isEmaMomentumSellSignal()) return SignalType.EMA_MOMENTUM_SELL;
        if (signal.isEnsembleBuySignal()) return SignalType.ENSEMBLE_BUY;
        if (signal.isEnsembleSellSignal()) return SignalType.ENSEMBLE_SELL;
        if (signal.isEnhancedEnsembleBuySignal()) return SignalType.ENHANCED_ENSEMBLE_BUY;
        if (signal.isEnhancedEnsembleSellSignal()) return SignalType.ENHANCED_ENSEMBLE_SELL;
        return SignalType.UNKNOWN;
    }

    private BigDecimal getCurrentPrice(String market) {
        List<TickerResponse> tickers = upbitQuotationClient.getTickers(List.of(market));
        TickerResponse ticker = tickers.get(0);
        return new BigDecimal(ticker.tradePrice());
    }
} 