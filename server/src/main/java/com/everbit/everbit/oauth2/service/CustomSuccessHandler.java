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
    private final RefreshTokenService refreshTokenService;

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

        // Access 토큰 생성 (15분)
        String accessToken = jwtUtil.createJwt("access", username, role);
        
        // Refresh 토큰 생성 (24시간)
        String refreshToken = jwtUtil.createJwt("refresh", username, role);
        
        // Redis에 refresh 토큰 저장
        refreshTokenService.saveRefreshToken(username, refreshToken);
            
        // Refresh 토큰만 httpOnly 쿠키로 설정
        ResponseCookie refreshCookie = createCookie("RefreshToken", refreshToken, 86400); // 24시간
        response.setHeader("Set-Cookie", refreshCookie.toString());
        
        // Access 토큰은 URL 파라미터로 전달 (클라이언트에서 로컬 스토리지에 저장)
        String redirectUrl = authenticatedRedirectUri + "?accessToken=" + accessToken;
        response.sendRedirect(redirectUrl);
    }

    private ResponseCookie createCookie(String name, String value, int maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .path("/")
                .secure(true)
                .httpOnly(true)
                .sameSite("None")
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }
}