package com.everbit.everbit.upbit.batch;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LossAndProfitManagementItemReader implements ItemReader<User> {
    
    private final UserService userService;
    private Iterator<User> userIterator;
    private boolean initialized = false;

    @Override
    public User read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        // 첫 호출 시 활성 사용자 목록 조회
        if (!initialized) {
            List<User> activeUsers = userService.findUsersWithActiveBots();
            log.info("활성 사용자 수: {}", activeUsers.size());
            userIterator = activeUsers.iterator();
            initialized = true;
        }

        // 다음 사용자 반환
        if (userIterator != null && userIterator.hasNext()) {
            return userIterator.next();
        }

        // 모든 사용자 처리 완료
        initialized = false;
        return null;
    }
}
