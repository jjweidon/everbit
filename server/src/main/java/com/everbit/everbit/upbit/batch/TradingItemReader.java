package com.everbit.everbit.upbit.batch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.upbit.batch.dto.UserMarketPair;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradingItemReader implements ItemReader<UserMarketPair> {
    
    private final UserService userService;
    private Iterator<UserMarketPair> pairIterator;
    private boolean initialized = false;

    @Override
    public UserMarketPair read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!initialized) {
            List<User> activeUsers = userService.findUsersWithActiveBots();
            List<UserMarketPair> pairs = new ArrayList<>();
            
            for (User user : activeUsers) {
                for (Market market : user.getBotSetting().getMarketList()) {
                    pairs.add(new UserMarketPair(user, market));
                }
            }
            
            log.info("트레이딩 배치 처리 대상: {}명의 사용자, {}개의 사용자-마켓 조합", activeUsers.size(), pairs.size());
            pairIterator = pairs.iterator();
            initialized = true;
        }

        if (pairIterator != null && pairIterator.hasNext()) {
            return pairIterator.next();
        }

        initialized = false;
        return null;
    }
}
