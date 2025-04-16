package com.everbit.everbit.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Upbit API 설정
 */
@Getter
@Setter
@Configuration
public class UpbitConfig {
    
    /**
     * Upbit API 기본 URL
     */
//    @Value("${upbit.api.base-url}")
    private final String baseUrl = "https://api.upbit.com";
    
    /**
     * Upbit API 액세스 키
     */
//    @Value("${upbit.api.access-key}")
    private String accessKey;
    
    /**
     * Upbit API 시크릿 키
     */
//    @Value("${upbit.api.secret-key}")
    private String secretKey;
} 