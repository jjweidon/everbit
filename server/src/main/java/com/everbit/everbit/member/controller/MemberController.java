package com.everbit.everbit.member.controller;

import com.everbit.everbit.global.dto.ApiResponse;
import com.everbit.everbit.global.dto.Response;
import com.everbit.everbit.member.dto.MemberResponse;
import com.everbit.everbit.member.dto.UpbitApiKeyRequest;
import com.everbit.everbit.member.entity.Member;
import com.everbit.everbit.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/me")
    public ApiResponse<Response> getCurrentMember(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return ApiResponse.failure("인증되지 않은 사용자입니다.");
        }
        String username = oAuth2User.getName();
        MemberResponse response = memberService.getMemberResponse(username);
        return ApiResponse.success(response, "현재 사용자 정보 조회 성공");
    }

    @PostMapping("/upbit-keys")
    public ApiResponse<Response> saveUpbitApiKeys(
            @RequestBody UpbitApiKeyRequest request,
            @AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return ApiResponse.failure("인증된 사용자가 아닙니다.");
        }

        String username = oAuth2User.getName();
        Member member = memberService.findMemberByUsername(username);

        memberService.saveUpbitApiKeys(member.getId(), request.getAccessKey(), request.getSecretKey());

        return ApiResponse.success(null, "업빗 API 키 저장 성공");
    }
} 