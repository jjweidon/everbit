package com.everbit.everbit.trade.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.everbit.everbit.oauth2.dto.CustomOAuth2User;
import com.everbit.everbit.trade.dto.TradeResponse;
import com.everbit.everbit.trade.service.TradeManager;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trades")
public class TradeController {
    private final TradeManager tradeManager;

    @GetMapping
    public ResponseEntity<List<TradeResponse>> getTrades(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        String username = oAuth2User.getName();
        log.info("GET 거래 내역 조회: {}", username);
        return ResponseEntity.ok(tradeManager.getTrades(username));
    }
}
