package com.everbit.everbit.controller;

import com.everbit.everbit.service.UpbitClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 업비트 API 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/upbit")
public class UpbitController {

    private final UpbitClient upbitClient;

    /**
     * 계좌 정보 조회
     */
    @GetMapping("/accounts")
    public ResponseEntity<?> getAccounts(
            @AuthenticationPrincipal OAuth2User oAuth2User) {
        String username = oAuth2User.getAttribute("name") + " " + oAuth2User.getAttribute("id");
        log.info("GET 계좌 정보 조회");
        try {
            String response = upbitClient.getAccounts(username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("계좌 정보 조회 오류", e);
            return ResponseEntity.badRequest().body("계좌 정보 조회 중 오류가 발생했습니다.");
        }
    }
} 