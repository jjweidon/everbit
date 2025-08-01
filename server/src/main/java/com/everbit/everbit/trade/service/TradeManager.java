package com.everbit.everbit.trade.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.everbit.everbit.trade.dto.StrategyResponse;
import com.everbit.everbit.trade.dto.TradeResponse;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeManager {
    private final TradeService tradeService;
    private final UserService userService;

    public List<TradeResponse> getTrades(String username) {
        User user = userService.findUserByUsername(username);
        return TradeResponse.from(tradeService.getTrades(user));
    }

    public List<StrategyResponse> getStrategies() {
        return StrategyResponse.getUserConfigurableStrategies();
    }
}
