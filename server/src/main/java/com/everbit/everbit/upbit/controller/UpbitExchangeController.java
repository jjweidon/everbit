package com.everbit.everbit.upbit.controller;

import com.everbit.everbit.global.dto.ApiResponse;
import com.everbit.everbit.oauth2.dto.CustomOAuth2User;
import com.everbit.everbit.upbit.dto.exchange.*;
import com.everbit.everbit.upbit.service.UpbitExchangeClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upbit")
public class UpbitExchangeController {

    private final UpbitExchangeClient upbitClient;

    // 전체 계좌 조회
    @GetMapping("/accounts")
    public ApiResponse<List<AccountResponse>> getAccounts(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        String username = oAuth2User.getName();
        log.info("GET 전체 계좌 조회");
        List<AccountResponse> response = upbitClient.getAccounts(username);
        return ApiResponse.success(response, "전체 계좌 조회 성공");
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

    // 미체결 주문 조회
    @GetMapping("/orders/open")
    public ApiResponse<List<OrderResponse>> getOpenOrders(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @RequestParam(required = false) String market,
            @RequestParam(name = "states[]", required = false) List<String> states) {
        String username = oAuth2User.getName();
        log.info("GET 미체결 주문 조회 - market: {}, states: {}", market, states);
        List<OrderResponse> response = upbitClient.getOpenOrders(username, market, states);
        return ApiResponse.success(response, "미체결 주문 조회 성공");
    }

    // 체결된 주문 조회
    @GetMapping("/orders/closed")
    public ApiResponse<List<OrderResponse>> getClosedOrders(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @RequestParam(required = false) String market,
            @RequestParam(name = "states[]", required = false) List<String> states) {
        String username = oAuth2User.getName();
        log.info("GET 체결된 주문 조회 - market: {}, states: {}", market, states);
        List<OrderResponse> response = upbitClient.getClosedOrders(username, market, states);
        return ApiResponse.success(response, "체결된 주문 조회 성공");
    }

    // 주문 생성
    @PostMapping("/orders")
    public ApiResponse<OrderResponse> createOrder(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @Valid @RequestBody OrderRequest request) {
        String username = oAuth2User.getName();
        log.info("POST 주문 생성 - market: {}, side: {}, type: {}", 
                request.market(), request.side(), request.ordType());
        OrderResponse response = upbitClient.createOrder(username, request);
        return ApiResponse.success(response, "주문 생성 성공");
    }

    // 주문 취소 후 재주문
    @PostMapping("/orders/cancel_and_new")
    public ApiResponse<ReplaceOrderResponse> replaceOrder(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @Valid @RequestBody ReplaceOrderRequest request) {
        String username = oAuth2User.getName();
        log.info("POST 주문 취소 후 재주문 - prevOrderUuid: {}, newOrdType: {}", 
                request.prevOrderUuid(), request.newOrdType());
        ReplaceOrderResponse response = upbitClient.replaceOrder(username, request);
        return ApiResponse.success(response, "주문 취소 후 재주문 성공");
    }

    // 주문 취소
    @DeleteMapping("/order")
    public ApiResponse<OrderResponse> cancelOrder(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User,
            @RequestParam(required = false) String uuid,
            @RequestParam(required = false) String identifier) {
        String username = oAuth2User.getName();
        log.info("DELETE 주문 취소 - uuid: {}, identifier: {}", uuid, identifier);
        OrderResponse response = upbitClient.cancelOrder(username, uuid, identifier);
        return ApiResponse.success(response, "주문 취소 성공");
    }
} 