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
import java.util.Iterator;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    
    // 토큰 유효기간 설정 (60시간)
    private static final long JWT_EXPIRATION_MS = 60 * 60 * 60 * 1000L; // 밀리초 단위
    private static final int COOKIE_MAX_AGE_SECONDS = 60 * 60 * 60; // 초 단위

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            log.info("OAuth2 인증 성공 처리");
            
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
            
            // JWT 토큰 생성 (밀리초 단위로 유효기간 전달)
            String token = jwtUtil.createJwt(username, role, JWT_EXPIRATION_MS);
            
            // 쿠키 생성 및 설정
            Cookie cookie = createCookie("Authorization", token);
            response.addCookie(cookie);
            
            // 클라이언트 리다이렉트
            String redirectUrl = "https://www.everbit.kr/";
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("OAuth2 인증 성공 처리 중 오류 발생", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "인증 처리 중 오류가 발생했습니다.");
        }
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS); // 60시간 (초 단위)
        cookie.setSecure(true); // HTTPS에서만 전송
        cookie.setPath("/");
        cookie.setHttpOnly(true); // 자바스크립트에서 접근 불가
        cookie.setDomain("everbit.kr"); // 도메인 설정
        
        return cookie;
    }
}