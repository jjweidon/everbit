package com.everbit.everbit.user.application;

/**
 * 복호화된 Upbit API 키. 서비스 계층에서만 사용하고 로그/DTO에 남기지 않는다.
 */
public record DecryptedUpbitCredentials(String accessKey, String secretKey) {}
