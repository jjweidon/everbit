package com.everbit.everbit.upbit.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.everbit.everbit.trade.entity.Trade;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class OrderStatusJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TaskExecutor batchTaskExecutor;
    private final OrderStatusItemReader itemReader;
    private final OrderStatusItemProcessor itemProcessor;
    private final OrderStatusItemWriter itemWriter;

    private static final int CHUNK_SIZE = 50;

    @Bean
    public Job orderStatusJob() {
        return new JobBuilder("orderStatusJob", jobRepository)
            .start(orderStatusStep())
            .build();
    }

    @Bean
    public Step orderStatusStep() {
        return new StepBuilder("orderStatusStep", jobRepository)
            .<Trade, Trade>chunk(CHUNK_SIZE, transactionManager)
            .reader(itemReader)
            .processor(itemProcessor)
            .writer(itemWriter)
            .taskExecutor(batchTaskExecutor)
            .build();
    }
}
