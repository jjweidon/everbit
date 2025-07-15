package com.everbit.everbit.upbit.controller;

import com.everbit.everbit.upbit.service.UpbitClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upbit")
public class UpbitController {

    private final UpbitClient upbitClient;

    // 계좌 정보 조회
    @GetMapping("/accounts")
    public ResponseEntity<?> getAccounts(
            @AuthenticationPrincipal OAuth2User oAuth2User) {
        String username = oAuth2User.getName();
        log.info("GET 계좌 정보 조회");
        String response = upbitClient.getAccounts(username);
        return ResponseEntity.ok(response);
    }
} 