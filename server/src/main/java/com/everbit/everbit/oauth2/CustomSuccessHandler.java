package com.everbit.everbit.oauth2;

import com.everbit.everbit.dto.CustomOAuth2User;
import com.everbit.everbit.jwt.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 인증 성공 처리 시작 - URL: {}", request.getRequestURL());
        
        // 요청 정보 로깅
        String clientIp = getClientIp(request);
        log.info("클라이언트 IP: {}", clientIp);
        
        // 요청 헤더 로깅
        log.debug("요청 헤더:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.debug("  - {}: {}", headerName, request.getHeader(headerName));
        }
        
        // 요청 파라미터 로깅
        log.debug("요청 파라미터:");
        request.getParameterMap().forEach((key, values) -> {
            if ("code".equals(key)) {
                String code = values[0];
                log.debug("  - code: {}", code.substring(0, Math.min(10, code.length())) + "...");
            } else {
                log.debug("  - {}: {}", key, String.join(", ", values));
            }
        });
        
        try {
            // OAuth2User 정보 추출
            if (!(authentication.getPrincipal() instanceof CustomOAuth2User)) {
                log.error("인증 객체가 CustomOAuth2User 타입이 아닙니다: {}", authentication.getPrincipal().getClass().getName());
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "인증 처리 중 오류가 발생했습니다.");
                return;
            }
            
            CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
            String username = customUserDetails.getName();
            log.info("인증된 사용자: {}", username);
            
            // 권한 정보 추출
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (authorities.isEmpty()) {
                log.warn("사용자 권한이 없습니다.");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "권한이 없습니다.");
                return;
            }
            
            Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
            GrantedAuthority auth = iterator.next();
            String role = auth.getAuthority();
            log.info("사용자 권한: {}", role);
            
            // JWT 토큰 생성
            String token = jwtUtil.createJwt(username, role, 60*60*60L);
            log.info("JWT 토큰 생성 완료: {}", token.substring(0, Math.min(10, token.length())) + "...");
            
            // 쿠키 생성 및 설정
            Cookie cookie = createCookie("Authorization", token);
            response.addCookie(cookie);
            log.info("인증 쿠키 설정 완료");
            
            // 클라이언트 리다이렉트
            String redirectUrl = "https://www.everbit.kr/";
            log.info("클라이언트로 리다이렉트: {}", redirectUrl);
            response.sendRedirect(redirectUrl);
            log.info("OAuth2 인증 성공 처리 완료");
        } catch (Exception e) {
            log.error("OAuth2 인증 성공 처리 중 오류 발생", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "인증 처리 중 오류가 발생했습니다.");
        }
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60*60*60); // 60시간
        cookie.setSecure(true); // HTTPS에서만 전송
        cookie.setPath("/");
        cookie.setHttpOnly(true); // 자바스크립트에서 접근 불가
        cookie.setDomain("everbit.kr"); // 도메인 설정
        
        return cookie;
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}