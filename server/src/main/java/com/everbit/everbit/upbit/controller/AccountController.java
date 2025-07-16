package com.everbit.everbit.upbit.controller;

import com.everbit.everbit.global.dto.ApiResponse;
import com.everbit.everbit.global.dto.Response;
import com.everbit.everbit.oauth2.dto.CustomOAuth2User;
import com.everbit.everbit.user.dto.UpbitKeyRequest;
import com.everbit.everbit.user.dto.UserResponse;
import com.everbit.everbit.upbit.service.AccountManager;
import com.everbit.everbit.upbit.service.UpbitClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

    private final UpbitClient upbitClient;
    private final AccountManager accountManager;

    // 업비트 API 키 저장
    @PostMapping
    public ApiResponse<Response> saveUpbitApiKeys(
            @RequestBody UpbitKeyRequest request,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        String username = oAuth2User.getName();
        log.info("POST 업비트 API 키 저장: {}", username);
        UserResponse response = accountManager.createAccount(username, request);
        return ApiResponse.success(response, "업비트 API 키 저장 성공");
    }

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