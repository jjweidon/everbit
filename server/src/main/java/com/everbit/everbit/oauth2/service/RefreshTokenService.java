package com.everbit.everbit.oauth2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${spring.jwt.refresh-expiration}")
    private long refreshExpiration;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    public void saveRefreshToken(String username, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + username;
        // Redis에 refresh 토큰 저장 (TTL: refresh 토큰 만료 시간)
        redisTemplate.opsForValue().set(key, refreshToken, refreshExpiration, TimeUnit.MILLISECONDS);
        log.info("Refresh token saved for user: {}", username);
    }

    public String getRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        String refreshToken = redisTemplate.opsForValue().get(key);
        log.debug("Refresh token retrieved for user: {}", username);
        return refreshToken;
    }

    public void deleteRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        redisTemplate.delete(key);
        log.info("Refresh token deleted for user: {}", username);
    }

    public boolean validateRefreshToken(String username, String refreshToken) {
        String storedToken = getRefreshToken(username);
        return storedToken != null && storedToken.equals(refreshToken);
    }
}
