package com.everbit.everbit.support.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.everbit.everbit.global.dto.ApiResponse;
import com.everbit.everbit.oauth2.dto.CustomOAuth2User;
import com.everbit.everbit.support.dto.InquiryRequest;
import com.everbit.everbit.support.service.SupportManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SupportController {
    private final SupportManager supportManager;

    // 문의 접수
    @PostMapping("/inquiries")
    public ApiResponse<Void> submitInquiry(
        @RequestBody InquiryRequest request,
        @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        String username = oAuth2User.getName();
        log.info("POST 문의 접수: {}", username);
        supportManager.submitInquiry(username, request);
        return ApiResponse.success(null, "문의 접수 성공");
    }
}
