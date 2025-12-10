package com.everbit.everbit.upbit.batch;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.everbit.everbit.upbit.service.LossAndProfitManagementService;
import com.everbit.everbit.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LossAndProfitManagementItemProcessor implements ItemProcessor<User, User> {
    
    private final LossAndProfitManagementService lossAndProfitManagementService;

    @Override
    public User process(User user) throws Exception {
        try {
            log.info("사용자 처리 시작: {}", user.getUsername());
            boolean success = lossAndProfitManagementService.processUserLossAndProfitManagement(user);
            
            if (success) {
                log.info("사용자 처리 완료: {}", user.getUsername());
            } else {
                log.warn("사용자 처리 실패: {}", user.getUsername());
            }
            
            return user;
        } catch (Exception e) {
            log.error("사용자 처리 중 오류 발생: {}", user.getUsername(), e);
            // 예외가 발생해도 다음 아이템 처리를 계속하기 위해 예외를 던지지 않음
            // 필요시 예외를 던져서 Skip 처리하거나 실패 처리할 수 있음
            return user;
        }
    }
}
