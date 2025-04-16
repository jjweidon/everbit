package com.everbit.everbit.controller;

import com.everbit.everbit.dto.UpbitApiKeyRequest;
import com.everbit.everbit.entity.Member;
import com.everbit.everbit.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/upbit-keys")
    public ResponseEntity<?> saveUpbitApiKeys(
            @RequestBody UpbitApiKeyRequest request,
            @AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return ResponseEntity.badRequest().body("인증된 사용자가 아닙니다.");
        }

        String username = oAuth2User.getName();
        Member member = memberService.getMemberByUsername(username);
        
        memberService.saveUpbitApiKeys(member.getId(), request.getAccessKey(), request.getSecretKey());
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentMember(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return ResponseEntity.status(401).body("인증되지 않은 사용자입니다.");
        }

        String username = oAuth2User.getName();
        Member member = memberService.getMemberByUsername(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", member.getId());
        response.put("username", member.getUsername());
        response.put("upbitAccessKey", member.getUpbitAccessKey());
        response.put("upbitSecretKey", member.getUpbitSecretKey());
        
        return ResponseEntity.ok(response);
    }
} 