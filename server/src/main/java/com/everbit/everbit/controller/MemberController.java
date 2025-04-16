package com.everbit.everbit.controller;

import com.everbit.everbit.dto.UpbitApiKeyRequest;
import com.everbit.everbit.entity.Member;
import com.everbit.everbit.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/upbit-keys")
    public ResponseEntity<?> saveUpbitApiKeys(
            @RequestBody UpbitApiKeyRequest request,
            @AuthenticationPrincipal OAuth2User oAuth2User) {
        String username = oAuth2User.getAttribute("name") + " " + oAuth2User.getAttribute("id");
        Member member = memberService.getMemberByUsername(username);
        
        memberService.saveUpbitApiKeys(member.getId(), request.getAccessKey(), request.getSecretKey());
        
        return ResponseEntity.ok().build();
    }
} 