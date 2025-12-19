package com.everbit.everbit.oauth2.controller;

import com.everbit.everbit.global.dto.ApiResponse;
import com.everbit.everbit.global.dto.Response;
import com.everbit.everbit.global.exception.CustomHttpStatus;
import com.everbit.everbit.global.jwt.JwtUtil;
import com.everbit.everbit.oauth2.dto.TokenResponse;
import com.everbit.everbit.oauth2.service.RefreshTokenService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Value("${spring.security.oauth2.logout-redirect-uri}")
    private String logoutRedirectUri;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Response>> refreshToken(
            @CookieValue(value = "RefreshToken", required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        try {
            // 쿠키에서 refresh 토큰이 없으면 요청 본문에서 확인
            if (refreshToken == null) {
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("RefreshToken".equals(cookie.getName())) {
                            refreshToken = cookie.getValue();
                            break;
                        }
                    }
                }
            }

            if (refreshToken == null) {
                log.warn("Refresh token not found");
                return ResponseEntity.ok(ApiResponse.failure("Refresh token이 없습니다.", CustomHttpStatus.UNAUTHENTICATED));
            }

            // Refresh 토큰 유효성 검증
            if (jwtUtil.isExpired(refreshToken)) {
                log.warn("Refresh token expired");
                // 만료된 refresh 토큰 쿠키 삭제
                deleteRefreshTokenCookie(response);
                return ResponseEntity.ok(ApiResponse.failure("Refresh token이 만료되었습니다.", CustomHttpStatus.UNAUTHENTICATED));
            }

            // Refresh 토큰 카테고리 확인
            String category = jwtUtil.getCategory(refreshToken);
            if (!"refresh".equals(category)) {
                log.warn("Invalid token category: {}", category);
                return ResponseEntity.ok(ApiResponse.failure("유효하지 않은 refresh token입니다.", CustomHttpStatus.UNAUTHENTICATED));
            }

            // Refresh 토큰에서 사용자 정보 추출
            String username = jwtUtil.getUsername(refreshToken);
            String role = jwtUtil.getRole(refreshToken);

            // Redis에서 저장된 refresh 토큰과 비교
            if (!refreshTokenService.validateRefreshToken(username, refreshToken)) {
                log.warn("Refresh token mismatch for user: {}", username);
                return ResponseEntity.ok(ApiResponse.failure("유효하지 않은 refresh token입니다.", CustomHttpStatus.UNAUTHENTICATED));
            }

            // 새로운 access 토큰 생성
            String newAccessToken = jwtUtil.createJwt("access", username, role);
            
            // 새로운 refresh 토큰 생성 (선택적: refresh 토큰 rotation)
            String newRefreshToken = jwtUtil.createJwt("refresh", username, role);
            refreshTokenService.saveRefreshToken(username, newRefreshToken);

            // Refresh 토큰만 httpOnly 쿠키로 설정
            ResponseCookie refreshCookie = createCookie("RefreshToken", newRefreshToken, 86400); // 24시간
            response.setHeader("Set-Cookie", refreshCookie.toString());

            // Access 토큰은 응답 본문에 포함 (클라이언트에서 로컬 스토리지에 저장)
            TokenResponse tokenResponse = new TokenResponse(newAccessToken, null);
            return ResponseEntity.ok(ApiResponse.success(tokenResponse, "토큰 갱신 성공"));

        } catch (Exception e) {
            log.error("Error refreshing token", e);
            return ResponseEntity.ok(ApiResponse.failure("토큰 갱신 중 오류가 발생했습니다.", CustomHttpStatus.INTERNAL_SERVER_ERROR));
        }
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

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("RefreshToken", null)
                .path("/")
                .maxAge(0)
                .build();
        response.setHeader("Set-Cookie", cookie.toString());
    }
}

