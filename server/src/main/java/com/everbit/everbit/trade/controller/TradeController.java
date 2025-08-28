package com.everbit.everbit.trade.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.everbit.everbit.oauth2.dto.CustomOAuth2User;
import com.everbit.everbit.global.dto.ApiResponse;
import com.everbit.everbit.trade.dto.MarketResponse;
import com.everbit.everbit.trade.dto.StrategyResponse;
import com.everbit.everbit.trade.dto.TradeResponse;
import com.everbit.everbit.trade.service.TradeFacade;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trades")
public class TradeController {
    private final TradeFacade tradeFacade;

    @GetMapping
    public ApiResponse<List<TradeResponse>> getDoneTrades(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        String username = oAuth2User.getName();
        log.info("GET 거래 내역 조회: {}", username);
        return ApiResponse.success(tradeFacade.getDoneTrades(username), "거래 내역 조회 성공");
    }

    @GetMapping("/strategies")
    public ApiResponse<List<StrategyResponse>> getStrategies() {
        log.info("GET 전략 목록 조회");
        return ApiResponse.success(tradeFacade.getStrategies(), "전략 목록 조회 성공");
    }

    @GetMapping("/markets")
    public ApiResponse<List<MarketResponse>> getMarkets() {
        log.info("GET 마켓 목록 조회");
        return ApiResponse.success(tradeFacade.getMarkets(), "마켓 목록 조회 성공");
    }
}
