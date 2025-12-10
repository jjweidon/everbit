package com.everbit.everbit.upbit.batch;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.everbit.everbit.trade.entity.Trade;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OrderStatusItemWriter implements ItemWriter<Trade> {

    @Override
    public void write(Chunk<? extends Trade> chunk) throws Exception {
        log.info("OrderStatus Chunk 처리 완료: {}개의 거래 처리됨", chunk.size());
    }
}
