package com.everbit.everbit.upbit.controller;

import com.everbit.everbit.global.dto.ApiResponse;
import com.everbit.everbit.oauth2.dto.CustomOAuth2User;
import com.everbit.everbit.upbit.dto.AccountResponse;
import com.everbit.everbit.upbit.dto.OrderChanceResponse;
import com.everbit.everbit.upbit.dto.OrderResponse;
import com.everbit.everbit.upbit.service.UpbitClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upbit")
public class UpbitController {

    private final UpbitClient upbitClient;

    // 계좌 정보 조회
    @GetMapping("/accounts")
    public ApiResponse<List<AccountResponse>> getAccounts(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        String username = oAuth2User.getName();
        log.info("GET 계좌 정보 조회");
        List<AccountResponse> response = upbitClient.getAccounts(username);
        return ApiResponse.success(response, "계좌 정보 조회 성공");
    }

    // 주문 가능 정보 조회
    @GetMapping("/orders/chance")
    public ApiResponse<OrderChanceResponse> getOrderChance(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @RequestParam String market) {
        String username = oAuth2User.getName();
        log.info("GET 주문 가능 정보 조회 - market: {}", market);
        OrderChanceResponse response = upbitClient.getOrderChance(username, market);
        return ApiResponse.success(response, "주문 가능 정보 조회 성공");
    }

    // 개별 주문 조회
    @GetMapping("/order")
    public ApiResponse<OrderResponse> getOrder(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @RequestParam(required = false) String uuid,
            @RequestParam(required = false) String identifier) {
        String username = oAuth2User.getName();
        log.info("GET 개별 주문 조회 - uuid: {}, identifier: {}", uuid, identifier);
        OrderResponse response = upbitClient.getOrder(username, uuid, identifier);
        return ApiResponse.success(response, "주문 조회 성공");
    }
} 