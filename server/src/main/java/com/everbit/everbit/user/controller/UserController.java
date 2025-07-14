package com.everbit.everbit.user.controller;

import com.everbit.everbit.global.dto.ApiResponse;
import com.everbit.everbit.global.dto.Response;
import com.everbit.everbit.oauth2.dto.CustomOAuth2User;
import com.everbit.everbit.user.dto.UserResponse;
import com.everbit.everbit.user.dto.UpbitApiKeyRequest;
import com.everbit.everbit.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<Response> getCurrentUser(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        String userId = oAuth2User.getId();
        UserResponse response = userService.getUserResponse(userId);
        return ApiResponse.success(response, "현재 사용자 정보 조회 성공");
    }

    @PutMapping("/upbit-keys")
    public ApiResponse<Response> saveUpbitApiKeys(
            @RequestBody UpbitApiKeyRequest request,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        String userId = oAuth2User.getId();
        UserResponse response = userService.saveUpbitApiKeys(userId, request);
        return ApiResponse.success(response, "업비트 API 키 저장 성공");
    }
} 