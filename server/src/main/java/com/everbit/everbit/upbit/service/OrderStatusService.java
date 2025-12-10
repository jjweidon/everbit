package com.everbit.everbit.upbit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.everbit.everbit.trade.entity.Trade;
import com.everbit.everbit.trade.entity.enums.TradeStatus;
import com.everbit.everbit.trade.service.TradeService;
import com.everbit.everbit.upbit.dto.exchange.OrderResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusService {
    
    private final TradeService tradeService;
    private final UpbitExchangeClient upbitExchangeClient;

    @Transactional
    public boolean checkAndUpdateOrderStatus(Trade trade) {
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
                    return true;
                case "done":
                    log.info("주문 체결 완료 - Trade ID: {}, Order UUID: {}", trade.getId(), trade.getOrderId());
                    tradeService.updateTradeStatus(trade, TradeStatus.DONE);
                    return true;
                default:
                    log.debug("주문 상태 변경 없음 - Trade ID: {}, Order UUID: {}, 상태: {}", 
                        trade.getId(), trade.getOrderId(), order.state());
                    return false;
            }
        } catch (Exception e) {
            log.error("주문 상태 확인 중 오류 발생 - Trade ID: {}, Order UUID: {}", 
                trade.getId(), trade.getOrderId(), e);
            return false;
        }
    }
}
