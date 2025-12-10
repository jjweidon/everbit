package com.everbit.everbit.upbit.batch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.everbit.everbit.trade.entity.Trade;
import com.everbit.everbit.upbit.service.OrderStatusService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusItemProcessor implements ItemProcessor<Trade, Trade> {
    
    private final OrderStatusService orderStatusService;

    @Override
    public Trade process(Trade trade) throws Exception {
        try {
            boolean updated = orderStatusService.checkAndUpdateOrderStatus(trade);
            if (updated) {
                log.info("Trade 상태 업데이트 완료 - Trade ID: {}", trade.getId());
            }
            return trade;
        } catch (Exception e) {
            log.error("Trade 상태 업데이트 실패 - Trade ID: {}", trade.getId(), e);
            return trade;
        }
    }
}
