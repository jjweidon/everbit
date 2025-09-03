package com.everbit.everbit.trade.controller;

import com.everbit.everbit.oauth2.dto.CustomOAuth2User;
import com.everbit.everbit.trade.dto.MarketResponse;
import com.everbit.everbit.trade.dto.StrategyResponse;
import com.everbit.everbit.trade.dto.TradeResponse;
import com.everbit.everbit.trade.service.TradeFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TradeControllerTest {

    @Mock
    private TradeFacade tradeFacade;

    @Mock
    private CustomOAuth2User oAuth2User;

    @InjectMocks
    private TradeController tradeController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private List<TradeResponse> testTradeResponses;
    private List<StrategyResponse> testStrategyResponses;
    private List<MarketResponse> testMarketResponses;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tradeController).build();
        objectMapper = new ObjectMapper();

        // 테스트 거래 응답 설정
        testTradeResponses = Arrays.asList(
            TradeResponse.builder()
                .tradeId("trade-1")
                .orderId("order-1")
                .market("BTC")
                .strategy("EXTREME_FLIP")
                .type("매수")
                .price(new BigDecimal("50000000"))
                .amount(new BigDecimal("0.001"))
                .totalPrice(new BigDecimal("50000"))
                .status("완료")
                .updatedAt(LocalDateTime.now())
                .build()
        );

        // 테스트 전략 응답 설정
        testStrategyResponses = Arrays.asList(
            StrategyResponse.builder()
                .name("EXTREME_FLIP")
                .value("extreme_flip")
                .description("극단적 반전 전략")
                .build()
        );

        // 테스트 마켓 응답 설정
        testMarketResponses = Arrays.asList(
            MarketResponse.builder()
                .market("BTC")
                .description("비트코인")
                .build(),
            MarketResponse.builder()
                .market("KRW")
                .description("원화")
                .build()
        );
    }

    @Test
    void getDoneTrades_완료된거래목록_거래응답리스트반환() throws Exception {
        // given
        String username = "test-user";
        when(oAuth2User.getName()).thenReturn(username);
        when(tradeFacade.getDoneTrades(username)).thenReturn(testTradeResponses);

        // when & then
        mockMvc.perform(get("/api/trades")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("거래 내역 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].tradeId").value("trade-1"))
                .andExpect(jsonPath("$.data[0].orderId").value("order-1"))
                .andExpect(jsonPath("$.data[0].market").value("BTC"));

        verify(tradeFacade, times(1)).getDoneTrades(username);
    }

    @Test
    void getDoneTrades_사용자거래없음_빈리스트반환() throws Exception {
        // given
        String username = "test-user";
        when(oAuth2User.getName()).thenReturn(username);
        when(tradeFacade.getDoneTrades(username)).thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/trades")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("거래 내역 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(tradeFacade, times(1)).getDoneTrades(username);
    }

    @Test
    void getStrategies_전략목록_전략응답리스트반환() throws Exception {
        // given
        when(tradeFacade.getStrategies()).thenReturn(testStrategyResponses);

        // when & then
        mockMvc.perform(get("/api/trades/strategies")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("전략 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("EXTREME_FLIP"))
                .andExpect(jsonPath("$.data[0].value").value("extreme_flip"))
                .andExpect(jsonPath("$.data[0].description").value("극단적 반전 전략"));

        verify(tradeFacade, times(1)).getStrategies();
    }

    @Test
    void getMarkets_마켓목록_마켓응답리스트반환() throws Exception {
        // given
        when(tradeFacade.getMarkets()).thenReturn(testMarketResponses);

        // when & then
        mockMvc.perform(get("/api/trades/markets")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("마켓 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].market").value("BTC"))
                .andExpect(jsonPath("$.data[0].description").value("비트코인"))
                .andExpect(jsonPath("$.data[1].market").value("KRW"))
                .andExpect(jsonPath("$.data[1].description").value("원화"));

        verify(tradeFacade, times(1)).getMarkets();
    }

    @Test
    void getDoneTrades_인증사용자정보없음_에러응답반환() throws Exception {
        // given
        when(oAuth2User.getName()).thenReturn(null);

        // when & then
        mockMvc.perform(get("/api/trades")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(tradeFacade, times(1)).getDoneTrades(null);
    }
}
