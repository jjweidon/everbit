package com.everbit.everbit.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class OAuthController {

    /**
     * OAuth2 인증 코드 로깅을 위한 디버그용 엔드포인트
     * Spring Security의 OAuth2 클라이언트가 자동으로 처리하기 전에 코드 수신 여부를 확인합니다.
     */
    @GetMapping("/debug/code/kakao")
    public ResponseEntity<?> debugKakaoCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            HttpServletRequest request) {
        
        // 클라이언트 IP 주소 가져오기
        String clientIp = getClientIp(request);
        log.info("카카오 OAuth2 디버그 콜백 - 접속 IP: {}", clientIp);
        
        // 모든 요청 매개변수 로깅
        log.info("카카오 OAuth2 콜백 파라미터:");
        log.info("  - code: {}", code != null ? code.substring(0, Math.min(10, code.length())) + "..." : "null");
        log.info("  - state: {}", state);
        log.info("  - error: {}", error);
        log.info("  - error_description: {}", errorDescription);
        
        // 모든 요청 헤더 로깅
        log.debug("요청 헤더:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.debug("  - {}: {}", headerName, request.getHeader(headerName));
        }
        
        // 요청 정보 반환
        Map<String, String> response = new HashMap<>();
        response.put("status", code != null ? "success" : "failed");
        response.put("message", code != null ? "카카오 인증 코드 수신 성공" : "카카오 인증 코드 수신 실패");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 클라이언트 IP 주소를 가져옵니다.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
} 