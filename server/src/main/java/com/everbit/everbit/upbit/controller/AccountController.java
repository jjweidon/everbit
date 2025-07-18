package com.everbit.everbit.upbit.controller;

import com.everbit.everbit.global.dto.ApiResponse;
import com.everbit.everbit.oauth2.dto.CustomOAuth2User;
import com.everbit.everbit.upbit.service.UpbitClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upbit/accounts")
public class AccountController {

    private final UpbitClient upbitClient;

    // 계좌 정보 조회
    @GetMapping("/me")
    public ApiResponse<String> getAccounts(
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        String username = oAuth2User.getName();
        log.info("GET 계좌 정보 조회");
        String response = upbitClient.getAccounts(username);
        return ApiResponse.success(response, "계좌 정보 조회 성공");
    }
} 