package com.everbit.everbit.trade.service;

import java.util.List;
import java.util.Arrays;

import org.springframework.stereotype.Service;

import com.everbit.everbit.trade.dto.MarketResponse;
import com.everbit.everbit.trade.dto.StrategyResponse;
import com.everbit.everbit.trade.dto.TradeResponse;
import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeFacade {
    private final TradeService tradeService;
    private final UserService userService;

    public List<TradeResponse> getDoneTrades(String username) {
        User user = userService.findUserByUsername(username);
        return TradeResponse.from(tradeService.findDoneTradesByUser(user));
    }

    public List<StrategyResponse> getStrategies() {
        return StrategyResponse.getUserConfigurableStrategies();
    }

    public List<MarketResponse> getMarkets() {
        return Arrays.stream(Market.values())
            .map(MarketResponse::from)
            .toList();
    }
}
