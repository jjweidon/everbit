package com.everbit.everbit.upbit.scheduler;

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

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("prod")
@Order(2)
public class LossAndProfitManagementScheduler {
    
    private final JobLauncher jobLauncher;
    private final Job lossAndProfitManagementJob;

    @Scheduled(cron = "1 */3 * * * *")
    public void checkLossAndProfitManagement() {
        log.info("손실 및 이익 관리 배치 Job 실행 시작");
        
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
            
            jobLauncher.run(lossAndProfitManagementJob, jobParameters);
            
            log.info("손실 및 이익 관리 배치 Job 실행 완료");
        } catch (Exception e) {
            log.error("손실 및 이익 관리 배치 Job 실행 중 오류 발생", e);
        }
    }
}
