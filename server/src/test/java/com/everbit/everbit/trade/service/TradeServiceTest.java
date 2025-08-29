package com.everbit.everbit.trade.service;

import com.everbit.everbit.trade.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @InjectMocks
    TradeRepository tradeRepository;

    @Test
    void findTradesByUser() {
    }

    @Test
    void findDoneTradesByUser() {
    }

    @Test
    void findWaitingTrades() {
    }

    @Test
    void saveTrade() {
    }

    @Test
    void updateTradeStatus() {
    }

    @Test
    void findLastBuyByUserAndMarket() {
    }
}