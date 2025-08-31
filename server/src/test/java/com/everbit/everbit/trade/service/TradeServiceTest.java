package com.everbit.everbit.trade.service;

import com.everbit.everbit.trade.entity.Trade;
import com.everbit.everbit.trade.entity.enums.Market;
import com.everbit.everbit.trade.entity.enums.Strategy;
import com.everbit.everbit.trade.entity.enums.TradeStatus;
import com.everbit.everbit.trade.entity.enums.TradeType;
import com.everbit.everbit.trade.repository.TradeRepository;
import com.everbit.everbit.upbit.dto.exchange.OrderResponse;
import com.everbit.everbit.user.entity.User;
import com.everbit.everbit.user.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @InjectMocks
    private TradeService tradeService;

    private User testUser;
    private Trade testTrade;
    private OrderResponse testOrderResponse;

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
                .status(TradeStatus.WAIT)
                .build();

        // 테스트 주문 응답 설정
        testOrderResponse = new OrderResponse(
                "test-order-id",
                "bid",
                "limit",
                "50000000",
                "wait",
                "KRW-BTC",
                OffsetDateTime.now(),
                "0.001",
                "0.001",
                "0",
                "0",
                "0",
                "50000",
                "0",
                "0",
                0,
                "0",
                "0",
                null,
                null
        );
    }

    @Test
    void findTradesByUser_사용자거래목록_거래리스트반환() {
        // given
        List<Trade> expectedTrades = Arrays.asList(testTrade);
        when(tradeRepository.findByUser(testUser)).thenReturn(expectedTrades);

        // when
        List<Trade> result = tradeService.findTradesByUser(testUser);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTrade, result.get(0));
        verify(tradeRepository, times(1)).findByUser(testUser);
    }

    @Test
    void findTradesByUser_사용자거래없음_빈리스트반환() {
        // given
        when(tradeRepository.findByUser(testUser)).thenReturn(List.of());

        // when
        List<Trade> result = tradeService.findTradesByUser(testUser);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tradeRepository, times(1)).findByUser(testUser);
    }

    @Test
    void findDoneTradesByUser_완료된거래목록_완료거래리스트반환() {
        // given
        Trade doneTrade = Trade.builder()
                .user(testUser)
                .market(Market.BTC)
                .strategy(Strategy.EXTREME_FLIP)
                .type(TradeType.BID)
                .orderId("done-order-id")
                .price(new BigDecimal("50000000"))
                .amount(new BigDecimal("0.001"))
                .totalPrice(new BigDecimal("50000"))
                .status(TradeStatus.DONE)
                .build();
        
        List<Trade> expectedTrades = Arrays.asList(doneTrade);
        when(tradeRepository.findByUserAndStatus(testUser, TradeStatus.DONE)).thenReturn(expectedTrades);

        // when
        List<Trade> result = tradeService.findDoneTradesByUser(testUser);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TradeStatus.DONE, result.get(0).getStatus());
        verify(tradeRepository, times(1)).findByUserAndStatus(testUser, TradeStatus.DONE);
    }

    @Test
    void findWaitingTrades_대기중인거래목록_대기거래리스트반환() {
        // given
        List<Trade> waitingTrades = Arrays.asList(testTrade);
        when(tradeRepository.findByStatus(TradeStatus.WAIT)).thenReturn(waitingTrades);

        // when
        List<Trade> result = tradeService.findWaitingTrades();

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(TradeStatus.WAIT, result.get(0).getStatus());
        verify(tradeRepository, times(1)).findByStatus(TradeStatus.WAIT);
    }

    @Test
    void saveTrade_새거래저장_저장된거래반환() {
        // given
        String market = "KRW-BTC";
        Strategy strategy = Strategy.EXTREME_FLIP;
        BigDecimal price = new BigDecimal("50000000");
        
        when(tradeRepository.save(any(Trade.class))).thenReturn(testTrade);

        // when
        Trade result = tradeService.saveTrade(testUser, market, strategy, testOrderResponse, price);

        // then
        assertNotNull(result);
        assertEquals(testTrade.getOrderId(), result.getOrderId());
        assertEquals(testTrade.getMarket(), result.getMarket());
        verify(tradeRepository, times(1)).save(any(Trade.class));
    }

    @Test
    void updateTradeStatus_거래상태업데이트_상태변경성공() {
        // given
        TradeStatus newStatus = TradeStatus.DONE;
        when(tradeRepository.save(any(Trade.class))).thenReturn(testTrade);

        // when
        tradeService.updateTradeStatus(testTrade, newStatus);

        // then
        assertEquals(newStatus, testTrade.getStatus());
        verify(tradeRepository, times(1)).save(testTrade);
    }

    @Test
    void findLastBuyByUserAndMarket_마지막매수거래존재_매수거래반환() {
        // given
        when(tradeRepository.findFirstByUserAndMarketAndTypeOrderByCreatedAtDesc(
                testUser, Market.BTC, TradeType.BID))
                .thenReturn(Optional.of(testTrade));

        // when
        Trade result = tradeService.findLastBuyByUserAndMarket(testUser, Market.BTC);

        // then
        assertNotNull(result);
        assertEquals(testTrade.getOrderId(), result.getOrderId());
        assertEquals(TradeType.BID, result.getType());
        verify(tradeRepository, times(1))
                .findFirstByUserAndMarketAndTypeOrderByCreatedAtDesc(testUser, Market.BTC, TradeType.BID);
    }

    @Test
    void findLastBuyByUserAndMarket_마지막매수거래없음_RuntimeException발생() {
        // given
        when(tradeRepository.findFirstByUserAndMarketAndTypeOrderByCreatedAtDesc(
                testUser, Market.BTC, TradeType.BID))
                .thenReturn(Optional.empty());

        // when & then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.findLastBuyByUserAndMarket(testUser, Market.BTC);
        });
        assertTrue(exception.getMessage().contains("마지막 매수 거래를 찾을 수 없습니다"));
        verify(tradeRepository, times(1))
                .findFirstByUserAndMarketAndTypeOrderByCreatedAtDesc(testUser, Market.BTC, TradeType.BID);
    }
}