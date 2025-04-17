package com.everbit.everbit.global.jwt;

import com.everbit.everbit.oauth2.dto.CustomOAuth2User;
import com.everbit.everbit.member.dto.MemberDto;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Cookie들을 불러온 뒤 Authorization Key에 담긴 쿠키를 찾음
        String authorization = null;
        Cookie[] cookies = request.getCookies();
        
        // 쿠키가 null인 경우 체크
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("Authorization")) {
                    authorization = cookie.getValue();
                    break;
                }
            }
        }

        // Authorization 쿠키가 없는 경우
        if (authorization == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 검증 및 사용자 인증 처리
        try {
            // 토큰 만료 여부 확인
            if (jwtUtil.isExpired(authorization)) {
                log.debug("토큰이 만료되었습니다.");
                // 만료된 토큰 쿠키 제거
                response.addCookie(createExpiredCookie("Authorization"));
                
                filterChain.doFilter(request, response);
                return;
            }
            
            // 토큰에서 사용자 정보 추출
            String username = jwtUtil.getUsername(authorization);
            String role = jwtUtil.getRole(authorization);
            
            // 사용자 DTO 생성
            MemberDto userDto = MemberDto.builder()
                    .username(username)
                    .role(role)
                    .build();
            
            // UserDetails에 회원 정보 객체 담기
            CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDto);
            
            // 스프링 시큐리티 인증 토큰 생성
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    customOAuth2User, 
                    null, 
                    customOAuth2User.getAuthorities()
            );
            
            // 세션에 사용자 등록
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
        } catch (ExpiredJwtException e) {
            log.debug("만료된 JWT 토큰입니다.");
            // 만료된 토큰 쿠키 제거
            response.addCookie(createExpiredCookie("Authorization"));
            
        } catch (MalformedJwtException | SignatureException e) {
            log.debug("잘못된 JWT 서명입니다.");
            // 잘못된 토큰 쿠키 제거
            response.addCookie(createExpiredCookie("Authorization"));
            
        } catch (JwtException e) {
            log.debug("JWT 토큰 처리 중 오류가 발생했습니다: {}", e.getMessage());
            // 문제가 있는 토큰 쿠키 제거
            response.addCookie(createExpiredCookie("Authorization"));
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 쿠키를 제거하는 유틸리티 메서드
     */
    private Cookie createExpiredCookie(String key) {
        Cookie cookie = new Cookie(key, null);
        cookie.setMaxAge(0); // 즉시 만료
        cookie.setPath("/");
        cookie.setDomain("everbit.kr");
        return cookie;
    }
}