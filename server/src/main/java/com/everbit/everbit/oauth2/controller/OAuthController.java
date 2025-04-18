package com.everbit.everbit.oauth2.controller;

import com.everbit.everbit.global.dto.ApiResponse;
import com.everbit.everbit.oauth2.dto.KakaoLoginResponse;
import com.everbit.everbit.oauth2.service.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OAuthController {
    private final OAuthService oAuthService;

    @GetMapping("/api/login/kakao")
    public ApiResponse<KakaoLoginResponse> kakaoLogin() {
        return ApiResponse.success(oAuthService.kakaoLogin(), "카카오 로그인 URL이 생성되었습니다.");
    }
} 