package com.everbit.everbit.upbit.batch;

import java.util.Iterator;
import java.util.List;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import com.everbit.everbit.trade.entity.Trade;
import com.everbit.everbit.trade.service.TradeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusItemReader implements ItemReader<Trade> {
    
    private final TradeService tradeService;
    private Iterator<Trade> tradeIterator;
    private boolean initialized = false;

    @Override
    public Trade read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!initialized) {
            List<Trade> waitingTrades = tradeService.findWaitingTrades();
            log.info("주문 상태 확인 배치 처리 대상: {}개의 WAIT 상태 거래", waitingTrades.size());
            tradeIterator = waitingTrades.iterator();
            initialized = true;
        }

        if (tradeIterator != null && tradeIterator.hasNext()) {
            return tradeIterator.next();
        }

        initialized = false;
        return null;
    }
}
