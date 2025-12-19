package com.everbit.everbit.global.jwt;

import org.springframework.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.everbit.everbit.global.config.util.SecurityConstants;
import com.everbit.everbit.oauth2.dto.CustomOAuth2User;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Value("${spring.security.oauth2.logout-redirect-uri}")
    private String logoutRedirectUri;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // GET 요청이고 public URL인 경우에만 필터를 거치지 않음
        return HttpMethod.GET.matches(method) && SecurityConstants.isPublicUrl(path);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Authorization 헤더에서 Access 토큰 추출 (로컬 스토리지에서 가져온 토큰)
        String authorization = null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            authorization = authHeader.substring(7);
        }

        // Authorization 헤더 검증
        if (authorization == null) {
            log.debug("Token is null");
            filterChain.doFilter(request, response);
            return;
        }

        //토큰
        String token = authorization;

        //토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {
            log.debug("Token is expired");
            // 401 Unauthorized 응답 반환 (클라이언트에서 refresh 처리)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Access 토큰 카테고리 확인
        try {
            String category = jwtUtil.getCategory(token);
            if (!"access".equals(category)) {
                log.debug("Invalid token category: {}", category);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } catch (Exception e) {
            log.debug("Failed to get token category", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        log.info("token: {}", token);

        //토큰에서 username과 role 획득
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        //userDto를 생성하여 값 set - 토큰에서 추출한 role 사용
        TokenDto tokenDto = TokenDto.create(username, role);

        //UserDetails에 회원 정보 객체 담기
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(tokenDto);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}