package com.everbit.everbit.member.controller;

import com.everbit.everbit.global.dto.ApiResponse;
import com.everbit.everbit.global.dto.Response;
import com.everbit.everbit.member.dto.MemberResponse;
import com.everbit.everbit.member.dto.UpbitApiKeyRequest;
import com.everbit.everbit.member.service.MemberService;
import com.everbit.everbit.oauth2.dto.CustomOAuth2User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/me")
    public ApiResponse<Response> getCurrentMember(@AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        String memberId = oAuth2User.getId();
        MemberResponse response = memberService.getMemberResponse(memberId);
        return ApiResponse.success(response, "현재 사용자 정보 조회 성공");
    }

    @PutMapping("/upbit-keys")
    public ApiResponse<Response> saveUpbitApiKeys(
            @RequestBody UpbitApiKeyRequest request,
            @AuthenticationPrincipal CustomOAuth2User oAuth2User) {
        String memberId = oAuth2User.getId();
        MemberResponse response = memberService.saveUpbitApiKeys(memberId, request);
        return ApiResponse.success(response, "업비트 API 키 저장 성공");
    }
} 