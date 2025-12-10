package com.everbit.everbit.upbit.batch.dto;

import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.user.entity.User;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserMarketPair {
    private final User user;
    private final Market market;
}
