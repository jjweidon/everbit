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

import com.everbit.everbit.user.entity.User;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class LossAndProfitManagementJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TaskExecutor batchTaskExecutor;
    private final LossAndProfitManagementItemReader itemReader;
    private final LossAndProfitManagementItemProcessor itemProcessor;
    private final LossAndProfitManagementItemWriter itemWriter;

    private static final int CHUNK_SIZE = 10; // 한 번에 처리할 사용자 수

    @Bean
    public Job lossAndProfitManagementJob() {
        return new JobBuilder("lossAndProfitManagementJob", jobRepository)
            .start(lossAndProfitManagementStep())
            .build();
    }

    @Bean
    public Step lossAndProfitManagementStep() {
        return new StepBuilder("lossAndProfitManagementStep", jobRepository)
            .<User, User>chunk(CHUNK_SIZE, transactionManager)
            .reader(itemReader)
            .processor(itemProcessor)
            .writer(itemWriter)
            .taskExecutor(batchTaskExecutor) // 병렬 처리
            .build();
    }
}
