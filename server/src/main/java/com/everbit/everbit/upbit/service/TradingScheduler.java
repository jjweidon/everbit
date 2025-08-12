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
import com.everbit.everbit.trade.entity.enums.SignalType;
import com.everbit.everbit.user.entity.BotSetting;

import java.util.List;
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
    private final UpbitExchangeClient upbitExchangeClient;
    private final UpbitQuotationClient upbitQuotationClient;
    private final UserService userService;
    private final TradeService tradeService;
    
    @Transactional
    @Scheduled(cron = "2 */3 * * * *")
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
        BotSetting botSetting = user.getBotSetting();
        Strategy buyStrategy = botSetting.getBuyStrategy();
        Strategy sellStrategy = botSetting.getSellStrategy();
        
        // 매수/매도 시그널 강도 계산
        double buySignalStrength = tradingSignalService.calculateSignalStrength(signal, buyStrategy);
        double sellSignalStrength = tradingSignalService.calculateSignalStrength(signal, sellStrategy);
        
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
                "]", 
                user.getUsername(), market,
                signal.currentPrice().doubleValue(),
                signal.bbLowerBand().doubleValue(),
                signal.bbMiddleBand().doubleValue(),
                signal.bbUpperBand().doubleValue(),
                signal.rsiValue().doubleValue(),
                signal.macdValue().doubleValue(),
                signal.macdSignalValue().doubleValue(),
                signal.macdHistogram().doubleValue(),
                signal.dropNFlipSignal());
        
        // 전략별 매수/매도 시그널 결정
        boolean buySignal = determineBuySignal(signal, buyStrategy);
        boolean sellSignal = determineSellSignal(signal, sellStrategy);

         // 개별 지표 시그널 상태 로깅
         log.info("사용자: {}, 마켓: {} - 개별 지표 시그널 [\n" +
                "  BB: 매수={}, 매도={},\n" +
                "  RSI: 매수={}, 매도={},\n" +
                "  MACD: 매수={}, 매도={},\n" +
                "  최종 시그널: 매수={}, 매도={}\n" +
                "]", 
        user.getUsername(), market, 
        signal.bbBuySignal(), signal.bbSellSignal(),
        signal.rsiBuySignal(), signal.rsiSellSignal(),
        signal.macdBuySignal(), signal.macdSellSignal(),
        buySignal, sellSignal);
        
        // 시그널 상태 로그 출력
        if (!buySignal && !sellSignal) {
            log.info("사용자: {}, 마켓: {} - 매수전략: {}, 매도전략: {}, 시그널 없음 (매수강도: {}, 매도강도: {})", 
                    user.getUsername(), market, buyStrategy.getValue(), sellStrategy.getValue(), 
                    String.format("%.2f", buySignalStrength), String.format("%.2f", sellSignalStrength));
        }
        
        // 매수 주문 로직
        if (buySignal) {
            log.info("마켓: {} - 매수 시그널 감지됨 (전략: {}, 시그널 강도: {})", market, buyStrategy, String.format("%.2f", buySignalStrength));
            try {
                // 매수용 최소/최대 주문금액 가져오기
                BigDecimal buyBaseOrderAmount = BigDecimal.valueOf(botSetting.getBuyBaseOrderAmount());
                BigDecimal buyMaxOrderAmount = BigDecimal.valueOf(botSetting.getBuyMaxOrderAmount());
                
                // 1. 계좌 잔고 확인
                OrderChanceResponse orderChance = upbitExchangeClient.getOrderChance(user.getUsername(), market);
                BigDecimal availableBalance = new BigDecimal(orderChance.bidAccount().balance());
                log.info("마켓: {} - 계좌 잔고: {}", market, availableBalance);

                if (availableBalance.compareTo(buyBaseOrderAmount) < 0) {
                    log.info("마켓: {} - 계좌 잔고가 최소 주문 금액({}) 이하이므로 매수 건너뜀", market, buyBaseOrderAmount);
                    return;
                }
                
                // 2. 주문 금액 계산
                BigDecimal currentPrice = getCurrentPrice(market);
                BigDecimal buyAmount = tradingSignalService.calculateOrderAmountBySignalStrength(signal, buyStrategy, buyBaseOrderAmount, buyMaxOrderAmount);
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
                tradeService.saveTrade(user, market, buyStrategy, orderResponse, currentPrice);
                log.info("마켓: {} - 매수 주문 실행 및 저장 완료 (전략: {}). 주문 정보: [상태: {}, 수량: {}, 가격: {}, 주문금액: {}, 시그널: {}]", 
                    market, buyStrategy.getValue(),
                    orderResponse.state(), orderResponse.volume(), 
                    orderResponse.price(), orderResponse.executedFunds(), 
                    signalType.getDescription());
            } catch (Exception e) {
                log.error("사용자: {}, 마켓: {} - 매수 시그널 처리 실패", user.getUsername(), market, e);
            }
        }
        
        // 매도 주문 로직
        if (sellSignal) {
            log.info("마켓: {} - 매도 시그널 감지됨 (전략: {}, 시그널 강도: {})", market, sellStrategy, String.format("%.2f", sellSignalStrength));
            try {
                // 매도용 최소/최대 주문금액 가져오기
                BigDecimal sellBaseOrderAmount = BigDecimal.valueOf(botSetting.getSellBaseOrderAmount());
                BigDecimal sellMaxOrderAmount = BigDecimal.valueOf(botSetting.getSellMaxOrderAmount());
                
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
                BigDecimal sellAmount = tradingSignalService.calculateOrderAmountBySignalStrength(signal, sellStrategy, sellBaseOrderAmount, sellMaxOrderAmount);
                sellAmount = sellAmount.min(availableQuantity.multiply(currentPrice));

                // 3. 주문 수량 계산 및 주문 실행
                BigDecimal sellQuantity = sellAmount.divide(currentPrice, 8, RoundingMode.DOWN);

                // 4. 매도 후 남을 수량 계산
                BigDecimal remainingQuantity = availableQuantity.subtract(sellQuantity);
                BigDecimal remainingAmount = remainingQuantity.multiply(currentPrice);
                if (remainingAmount.compareTo(sellBaseOrderAmount) <= 0) {
                    sellQuantity = availableQuantity;
                    log.info("마켓: {} - 남은 주문 가능 금액 {}이 SELL_BASE_ORDER_AMOUNT({})이하이므로 전체 보유 수량 매도: {}", 
                        market, remainingAmount, sellBaseOrderAmount, sellQuantity.multiply(currentPrice));
                }

                // 5. 매도 주문 금액 계산 및 주문 실행
                sellAmount = sellQuantity.multiply(currentPrice);
                
                // BigDecimal을 일반 표기법으로 변환하여 주문 수량 문자열 생성
                String sellQuantityStr = sellQuantity.toPlainString();
                OrderRequest orderRequest = OrderRequest.createSellOrder(market, sellQuantityStr, currentPrice.toString());
                OrderResponse orderResponse = upbitExchangeClient.createOrder(user.getUsername(), orderRequest);

                // 6. 주문 결과 저장
                SignalType signalType = determineSignalType(signal);
                tradeService.saveTrade(user, market, sellStrategy, orderResponse, currentPrice);
                log.info("마켓: {} - 매도 주문 실행 및 저장 완료 (전략: {}). 주문 정보: [상태: {}, 수량: {}, 가격: {}, 주문금액: {}, 시그널: {}]", 
                    market, sellStrategy.getValue(),
                    orderResponse.state(), orderResponse.volume(), 
                    orderResponse.price(), orderResponse.executedFunds(), 
                    signalType.getDescription());
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
            case STANDARD:
                return signal.dropNFlipSignal();
            case TRIPLE_INDICATOR_CONSERVATIVE:
                return signal.isTripleIndicatorConservativeBuySignal();
            case TRIPLE_INDICATOR_MODERATE:
                return signal.isTripleIndicatorModerateBuySignal();
            case TRIPLE_INDICATOR_AGGRESSIVE:
                return signal.isTripleIndicatorAggressiveBuySignal();
            case BB_RSI_COMBO:
                return signal.isBbRsiComboBuySignal();
            case RSI_MACD_COMBO:
                return signal.isRsiMacdComboBuySignal();
            case BB_MACD_COMBO:
                return signal.isBbMacdComboBuySignal();
            default:
                return signal.dropNFlipSignal();
        }
    }
    
    /**
     * 사용자가 선택한 전략에 따라 매도 시그널을 결정합니다.
     */
    private boolean determineSellSignal(TradingSignal signal, Strategy strategy) {
        switch (strategy) {
            case STANDARD:
                return signal.isTripleIndicatorModerateSellSignal();
            case TRIPLE_INDICATOR_CONSERVATIVE:
                return signal.isTripleIndicatorConservativeSellSignal();
            case TRIPLE_INDICATOR_MODERATE:
                return signal.isTripleIndicatorModerateSellSignal();
            case TRIPLE_INDICATOR_AGGRESSIVE:
                return signal.isTripleIndicatorAggressiveSellSignal();
            case BB_RSI_COMBO:
                return signal.isBbRsiComboSellSignal();
            case RSI_MACD_COMBO:
                return signal.isRsiMacdComboSellSignal();
            case BB_MACD_COMBO:
                return signal.isBbMacdComboSellSignal();
            default:
                return signal.isTripleIndicatorModerateSellSignal();
        }
    }

    private SignalType determineSignalType(TradingSignal signal) {
        // 3지표 보수전략
        if (signal.isTripleIndicatorConservativeBuySignal()) return SignalType.TRIPLE_INDICATOR_CONSERVATIVE_BUY;
        if (signal.isTripleIndicatorConservativeSellSignal()) return SignalType.TRIPLE_INDICATOR_CONSERVATIVE_SELL;
        
        // 3지표 중간전략
        if (signal.isTripleIndicatorModerateBuySignal()) return SignalType.TRIPLE_INDICATOR_MODERATE_BUY;
        if (signal.isTripleIndicatorModerateSellSignal()) return SignalType.TRIPLE_INDICATOR_MODERATE_SELL;
        
        // 3지표 공격전략
        if (signal.isTripleIndicatorAggressiveBuySignal()) return SignalType.TRIPLE_INDICATOR_AGGRESSIVE_BUY;
        if (signal.isTripleIndicatorAggressiveSellSignal()) return SignalType.TRIPLE_INDICATOR_AGGRESSIVE_SELL;
        
        // 2지표 조합 전략들
        if (signal.isBbRsiComboBuySignal()) return SignalType.BB_RSI_COMBO_BUY;
        if (signal.isBbRsiComboSellSignal()) return SignalType.BB_RSI_COMBO_SELL;
        if (signal.isRsiMacdComboBuySignal()) return SignalType.RSI_MACD_COMBO_BUY;
        if (signal.isRsiMacdComboSellSignal()) return SignalType.RSI_MACD_COMBO_SELL;
        if (signal.isBbMacdComboBuySignal()) return SignalType.BB_MACD_COMBO_BUY;
        if (signal.isBbMacdComboSellSignal()) return SignalType.BB_MACD_COMBO_SELL;

        if (signal.dropNFlipSignal()) return SignalType.DROP_N_FLIP;
        
        return SignalType.UNKNOWN;
    }

    private BigDecimal getCurrentPrice(String market) {
        List<TickerResponse> tickers = upbitQuotationClient.getTickers(List.of(market));
        TickerResponse ticker = tickers.get(0);
        return new BigDecimal(ticker.tradePrice()).setScale(8, RoundingMode.HALF_UP);
    }
} 