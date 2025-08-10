package com.everbit.everbit.upbit.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.annotation.Order;

import com.everbit.everbit.trade.entity.Trade;
import com.everbit.everbit.trade.entity.enums.TradeStatus;
import com.everbit.everbit.trade.service.TradeService;
import com.everbit.everbit.upbit.dto.exchange.OrderResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("prod")
@Order(1)
public class OrderStatusScheduler {
    private final TradeService tradeService;
    private final UpbitExchangeClient upbitExchangeClient;

    @Transactional
    @Scheduled(cron = "0 */3 * * * *") // 3분마다 실행
    public void checkOrderStatus() {
        log.info("주문 상태 확인 스케줄러 실행");
        
        try {
            // WAIT 상태인 모든 거래 조회
            List<Trade> waitingTrades = tradeService.findWaitingTrades();
            log.info("WAIT 상태인 거래 수: {}", waitingTrades.size());

            for (Trade trade : waitingTrades) {
                try {
                    // 업비트 API를 통해 주문 상태 조회
                    OrderResponse order = upbitExchangeClient.getOrder(
                        trade.getUser().getUsername(),
                        trade.getOrderId(),
                        null
                    );

                    // 주문 상태에 따라 Trade 상태 업데이트
                    switch (order.state()) {
                        case "wait":
                            // 주문이 아직 대기 중이면 취소
                            log.info("대기 중인 주문 취소 - Trade ID: {}, Order UUID: {}", trade.getId(), trade.getOrderId());
                            upbitExchangeClient.cancelOrder(trade.getUser().getUsername(), trade.getOrderId(), null);
                            tradeService.updateTradeStatus(trade, TradeStatus.CANCEL);
                            break;
                        case "done":
                            log.info("주문 체결 완료 - Trade ID: {}, Order UUID: {}", trade.getId(), trade.getOrderId());
                            tradeService.updateTradeStatus(trade, TradeStatus.DONE);
                            break;
                    }
                } catch (Exception e) {
                    log.error("주문 상태 확인 중 오류 발생 - Trade ID: {}, Order UUID: {}", 
                        trade.getId(), trade.getOrderId(), e);
                }
            }
        } catch (Exception e) {
            log.error("주문 상태 확인 스케줄러 실행 중 오류 발생", e);
        }
    }
} 