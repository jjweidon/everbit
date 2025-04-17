package com.everbit.everbit.oauth2.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OAuthController {

    @GetMapping("/api/login/oauth2/code/kakao")
    public ResponseEntity<?> kakaoCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            HttpServletRequest request) {
        
        if (error != null) {
            log.error("카카오 OAuth2 인증 오류: {}", error);
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        
        // 이 엔드포인트는 Spring Security의 OAuth2 필터에 의해 처리될 것으로 예상됩니다.
        // 만약 이 메서드가 호출된다면 Spring Security 필터가 제대로 작동하지 않는 것입니다.
        log.warn("Spring Security OAuth2 필터가 처리하지 않은 OAuth2 콜백 요청을 수신했습니다.");
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "unhandled");
        response.put("message", "OAuth2 콜백이 Spring Security에 의해 처리되지 않았습니다. 설정을 확인하세요.");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/auth/me")
    public ResponseEntity<?> getAuthenticatedUser(HttpServletRequest request) {
        // 더미 응답 반환
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "인증된 사용자 정보 조회 성공");
        response.put("authenticated", request.getUserPrincipal() != null);
        response.put("principal", request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous");
        
        return ResponseEntity.ok(response);
    }
} 