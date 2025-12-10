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

import com.everbit.everbit.upbit.batch.dto.UserMarketPair;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class TradingJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TaskExecutor batchTaskExecutor;
    private final TradingItemReader itemReader;
    private final TradingItemProcessor itemProcessor;
    private final TradingItemWriter itemWriter;

    private static final int CHUNK_SIZE = 20;

    @Bean
    public Job tradingJob() {
        return new JobBuilder("tradingJob", jobRepository)
            .start(tradingStep())
            .build();
    }

    @Bean
    public Step tradingStep() {
        return new StepBuilder("tradingStep", jobRepository)
            .<UserMarketPair, UserMarketPair>chunk(CHUNK_SIZE, transactionManager)
            .reader(itemReader)
            .processor(itemProcessor)
            .writer(itemWriter)
            .taskExecutor(batchTaskExecutor)
            .build();
    }
}
