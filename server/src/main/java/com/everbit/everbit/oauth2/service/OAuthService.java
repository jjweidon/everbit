package com.everbit.everbit.oauth2.service;

import com.everbit.everbit.oauth2.dto.KakaoLoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {
    public KakaoLoginResponse kakaoLogin() {
        return new KakaoLoginResponse("/api/login/oauth2/code/kakao");
    }
} 