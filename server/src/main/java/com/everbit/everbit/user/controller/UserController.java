package com.everbit.everbit.user.controller;

import com.everbit.everbit.global.dto.ApiResponse;
import com.everbit.everbit.global.dto.Response;
import com.everbit.everbit.oauth2.dto.CustomOAuth2User;
import com.everbit.everbit.user.dto.EmailRequest;
import com.everbit.everbit.user.dto.UpbitApiKeysResponse;
import com.everbit.everbit.user.dto.UpbitKeyRequest;
import com.everbit.everbit.user.dto.UserResponse;
import com.everbit.everbit.user.service.UserManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserManager userManager;

    // 현재 사용자 정보 조회
    @GetMapping("/me")
    public ApiResponse<Response> getCurrentUser(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        String username = oAuth2User.getName();
        UserResponse response = userManager.getUserResponse(username);
        return ApiResponse.success(response, "현재 사용자 정보 조회 성공");
    }

    // 업비트 API 키 등록
    @PatchMapping("/me/upbit-keys")
    public ApiResponse<Response> registerUpbitApiKeys(
            @RequestBody UpbitKeyRequest request,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        String username = oAuth2User.getName();
        log.info("POST 업비트 API 키 등록: {}", username);
        UserResponse response = userManager.registerUpbitApiKeys(username, request);
        return ApiResponse.success(response, "업비트 API 키 등록 성공");
    }

    // 업비트 API 키 조회
    @GetMapping("/me/upbit-keys")
    public ApiResponse<Response> getUpbitApiKeys(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        String username = oAuth2User.getName();
        log.info("GET 업비트 API 키 조회: {}", username);
        UpbitApiKeysResponse response = userManager.getUpbitApiKeys(username);
        return ApiResponse.success(response, "업비트 API 키 조회 성공");
    }

    // 이메일 업데이트
    @PatchMapping("/me/email")
    public ApiResponse<Response> updateEmail(@AuthenticationPrincipal CustomOAuth2User oAuth2User, @RequestBody EmailRequest request) {
        String username = oAuth2User.getName();
        userManager.updateEmail(username, request.email());
        return ApiResponse.success(null, "이메일 업데이트 성공");
    }

    // 봇 활성화 토글
    @PatchMapping("/me/bot-active")
    public ApiResponse<Response> toggleBotActive(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        String username = oAuth2User.getName();
        userManager.toggleBotActive(username);
        return ApiResponse.success(null, "봇 활성화 토글 성공");
    }
} 