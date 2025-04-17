package com.everbit.everbit.global.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MainController {

    @GetMapping("/")
    public ResponseEntity<?> hello(HttpServletRequest request) {
        // 클라이언트 IP 주소 가져오기
        String clientIp = getClientIp(request);
        
        log.info("HELLO EVERBIT - 접속 IP: {}", clientIp);
        try {
            return ResponseEntity.ok().body("HELLO EVERBIT");
        } catch (Exception e) {
            log.error("HELLO ERROR - 접속 IP: {}", clientIp, e);
            return ResponseEntity.badRequest().body("HELLO ERROR🥲");
        }
    }
    
    /**
     * 클라이언트 IP 주소를 가져옵니다.
     * 프록시나 로드 밸런서 환경을 고려하여 X-Forwarded-For 헤더를 먼저 확인합니다.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For 헤더는 여러 IP를 포함할 수 있으며, 첫 번째가 원본 클라이언트 IP
            return xForwardedFor.split(",")[0].trim();
        }
        
        return request.getRemoteAddr();
    }
}
