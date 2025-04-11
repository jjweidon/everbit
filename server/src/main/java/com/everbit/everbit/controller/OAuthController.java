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
     * 실제 OAuth2 콜백 엔드포인트 - Spring Security의 OAuth2 처리를 보완하는 역할
     */
    @GetMapping("/login/oauth2/code/kakao")
    public ResponseEntity<?> kakaoCallback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            HttpServletRequest request) {
        
        String clientIp = getClientIp(request);
        log.info("카카오 OAuth2 콜백 처리 - 접속 IP: {}", clientIp);
        log.info("카카오 인증 코드: {}", code != null ? code.substring(0, Math.min(10, code.length())) + "..." : "null");
        log.info("상태 토큰: {}", state);
        
        if (error != null) {
            log.error("카카오 OAuth2 인증 오류: {}", error);
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }
        
        // 이 시점에서는 Spring Security의 OAuth2 클라이언트가 자동으로 처리해야 함
        // 만약 이 메서드가 호출된다면 Spring Security의 필터가 제대로 작동하지 않는 것임
        log.warn("Spring Security OAuth2 필터가 처리하지 않은 OAuth2 콜백 요청을 수신했습니다.");
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "unhandled");
        response.put("message", "OAuth2 콜백이 Spring Security에 의해 처리되지 않았습니다. 설정을 확인하세요.");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 오류 페이지 디버깅용 엔드포인트
     */
    @GetMapping("/error/debug")
    public ResponseEntity<?> debugError(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        log.info("에러 페이지 디버그 - 접속 IP: {}", clientIp);
        
        // 요청 정보 로깅
        log.info("요청 URL: {}", request.getRequestURL());
        log.info("요청 메서드: {}", request.getMethod());
        
        // 요청 헤더 로깅
        log.debug("요청 헤더:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.debug("  - {}: {}", headerName, request.getHeader(headerName));
        }
        
        // 요청 매개변수 로깅
        log.debug("요청 파라미터:");
        request.getParameterMap().forEach((key, values) -> {
            log.debug("  - {}: {}", key, String.join(", ", values));
        });
        
        return ResponseEntity.ok(Map.of(
            "status", "debug",
            "message", "에러 페이지 디버그 정보",
            "request_url", request.getRequestURL().toString(),
            "client_ip", clientIp
        ));
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