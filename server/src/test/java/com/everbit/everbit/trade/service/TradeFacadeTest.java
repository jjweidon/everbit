package com.everbit.everbit.trade.service;

import com.everbit.everbit.trade.dto.MarketResponse;
import com.everbit.everbit.trade.dto.StrategyResponse;
import com.everbit.everbit.trade.dto.TradeResponse;
import com.everbit.everbit.trade.entity.Trade;
import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.trade.entity.enums.Strategy;
import com.everbit.everbit.trade.entity.enums.TradeStatus;
import com.everbit.everbit.trade.entity.enums.TradeType;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.entity.enums.Role;
import com.everbit.everbit.user.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeFacadeTest {

    @Mock
    private TradeService tradeService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TradeFacade tradeFacade;

    private User testUser;
    private Trade testTrade;
    private List<Trade> testTrades;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 설정
        testUser = User.builder()
                .username("test-user")
                .role(Role.ROLE_USER)
                .nickname("테스트유저")
                .build();

        // 테스트 거래 설정
        testTrade = Trade.builder()
                .user(testUser)
                .market(Market.BTC)
                .strategy(Strategy.EXTREME_FLIP)
                .type(TradeType.BID)
                .orderId("test-order-id")
                .price(new BigDecimal("50000000"))
                .amount(new BigDecimal("0.001"))
                .totalPrice(new BigDecimal("50000"))
                .status(TradeStatus.DONE)
                .build();

        testTrades = Arrays.asList(testTrade);
    }

    @Test
    void getDoneTrades_완료된거래목록_거래응답리스트반환() {
        // given
        String username = "test-user";
        when(userService.findUserByUsername(username)).thenReturn(testUser);
        when(tradeService.findDoneTradesByUser(testUser)).thenReturn(testTrades);

        // when
        List<TradeResponse> result = tradeFacade.getDoneTrades(username);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTrade.getOrderId(), result.get(0).orderId());
        assertEquals(testTrade.getMarket().name(), result.get(0).market());
        verify(userService, times(1)).findUserByUsername(username);
        verify(tradeService, times(1)).findDoneTradesByUser(testUser);
    }

    @Test
    void getDoneTrades_사용자거래없음_빈리스트반환() {
        // given
        String username = "test-user";
        when(userService.findUserByUsername(username)).thenReturn(testUser);
        when(tradeService.findDoneTradesByUser(testUser)).thenReturn(List.of());

        // when
        List<TradeResponse> result = tradeFacade.getDoneTrades(username);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userService, times(1)).findUserByUsername(username);
        verify(tradeService, times(1)).findDoneTradesByUser(testUser);
    }

    @Test
    void getStrategies_사용자설정가능전략_전략응답리스트반환() {
        // when
        List<StrategyResponse> result = tradeFacade.getStrategies();

        // then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        // StrategyResponse.getUserConfigurableStrategies()가 반환하는 전략들이 포함되어 있는지 확인
        assertTrue(result.stream().anyMatch(strategy -> 
            strategy.name().equals(Strategy.EXTREME_FLIP.name())));
    }

    @Test
    void getMarkets_모든마켓_마켓응답리스트반환() {
        // when
        List<MarketResponse> result = tradeFacade.getMarkets();

        // then
        assertNotNull(result);
        assertEquals(Market.values().length, result.size());
        // 모든 Market enum 값들이 포함되어 있는지 확인
        Arrays.stream(Market.values()).forEach(market -> {
            assertTrue(result.stream().anyMatch(marketResponse -> 
                marketResponse.market().equals(market.name())));
        });
    }

    @Test
    void getMarkets_마켓목록_올바른마켓코드포함() {
        // when
        List<MarketResponse> result = tradeFacade.getMarkets();

        // then
        assertNotNull(result);
        // BTC 마켓이 포함되어 있는지 확인
        assertTrue(result.stream().anyMatch(market -> 
            market.market().equals("BTC")));
        // KRW 마켓이 포함되어 있는지 확인
        assertTrue(result.stream().anyMatch(market -> 
            market.market().equals("KRW")));
    }
}