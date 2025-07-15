package com.everbit.everbit.oauth2.service;

import com.everbit.everbit.oauth2.dto.CustomOAuth2User;
import com.everbit.everbit.global.jwt.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Value("${spring.security.oauth2.authenticated-redirect-uri}")
    private String authenticatedRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String username = customUserDetails.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        //토큰 생성
        String access = jwtUtil.createJwt("access", username, role);
        // String refresh = jwtUtil.createJwt("refresh", username, role, 86400000L);
            
        // ResponseCookie를 사용하여 쿠키 설정
        ResponseCookie cookie = createCookie("Authorization", access);
        response.setHeader("Set-Cookie", cookie.toString());
        log.info("토큰 쿠키 생성 완료: {}", access);
        log.info("리다이렉트: {}", authenticatedRedirectUri);
        response.sendRedirect(authenticatedRedirectUri);
    }

    private ResponseCookie createCookie(String name, String value) {
        return ResponseCookie.from(name, value)
                .path("/")
                .secure(true)
                .httpOnly(true)
                .sameSite("None")
                .maxAge(Duration.ofHours(24))
                .build();
    }
}