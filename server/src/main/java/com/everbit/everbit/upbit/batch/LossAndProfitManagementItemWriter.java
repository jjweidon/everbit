package com.everbit.everbit.upbit.batch;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import com.everbit.everbit.user.entity.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LossAndProfitManagementItemWriter implements ItemWriter<User> {

    @Override
    public void write(Chunk<? extends User> chunk) throws Exception {
        log.info("Chunk 처리 완료: {}명의 사용자 처리됨", chunk.size());

        // TODO: 처리 결과를 별도 테이블에 저장, 메트릭 수집
    }
}
