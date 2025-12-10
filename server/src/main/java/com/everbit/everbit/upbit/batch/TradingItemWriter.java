package com.everbit.everbit.upbit.batch;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.everbit.everbit.upbit.batch.dto.UserMarketPair;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TradingItemWriter implements ItemWriter<UserMarketPair> {

    @Override
    public void write(Chunk<? extends UserMarketPair> chunk) throws Exception {
        log.info("Trading Chunk 처리 완료: {}개의 사용자-마켓 조합 처리됨", chunk.size());
    }
}
