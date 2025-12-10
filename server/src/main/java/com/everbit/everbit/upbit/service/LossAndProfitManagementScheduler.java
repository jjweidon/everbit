package com.everbit.everbit.upbit.service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 손실 및 이익 관리 스케줄러
 * Spring Batch Job을 주기적으로 실행하여 대규모 트래픽을 효율적으로 처리합니다.
 * 비즈니스 로직은 LossAndProfitManagementService와 Batch Job에서 처리됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("prod")
@Order(2)
public class LossAndProfitManagementScheduler {
    
    private final JobLauncher jobLauncher;
    private final Job lossAndProfitManagementJob;

    /**
     * 3분마다 손실 및 이익 관리 배치 Job을 실행합니다.
     * Spring Batch를 통해 병렬 처리 및 분산 처리가 가능합니다.
     */
    @Scheduled(cron = "1 */3 * * * *") // 3분마다 실행
    public void checkLossAndProfitManagement() {
        log.info("손실 및 이익 관리 배치 Job 실행 시작");
        
        try {
            // Job 실행을 위한 고유한 파라미터 생성 (동일한 Job의 중복 실행 방지)
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
            
            // Job 실행
            jobLauncher.run(lossAndProfitManagementJob, jobParameters);
            
            log.info("손실 및 이익 관리 배치 Job 실행 완료");
        } catch (Exception e) {
            log.error("손실 및 이익 관리 배치 Job 실행 중 오류 발생", e);
        }
    }
} 