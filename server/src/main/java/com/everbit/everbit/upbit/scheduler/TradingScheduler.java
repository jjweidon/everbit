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
@Order(3)
public class TradingScheduler {
    
    private final JobLauncher jobLauncher;
    private final Job tradingJob;

    @Scheduled(cron = "2 */10 * * * *")
    public void checkTradingSignals() {
        log.info("트레이딩 배치 Job 실행 시작");
        
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
            
            jobLauncher.run(tradingJob, jobParameters);
            
            log.info("트레이딩 배치 Job 실행 완료");
        } catch (Exception e) {
            log.error("트레이딩 배치 Job 실행 중 오류 발생", e);
        }
    }
}
