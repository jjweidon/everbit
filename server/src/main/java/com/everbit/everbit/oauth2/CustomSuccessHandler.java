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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 로그인 성공 처리");
        
        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String username = customUserDetails.getName();
        log.info("인증된 사용자: {}", username);

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        // JWT 토큰 생성 (유효기간: 60시간)
        String token = jwtUtil.createJwt(username, role, 60*60*60L);
        log.info("JWT 토큰 생성 완료");

        // 응답 쿠키에 토큰 추가
        response.addCookie(createCookie("Authorization", token));
        
        // 클라이언트 리다이렉트
        log.info("클라이언트로 리다이렉트: https://www.everbit.kr/");
        response.sendRedirect("https://www.everbit.kr/");
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
}