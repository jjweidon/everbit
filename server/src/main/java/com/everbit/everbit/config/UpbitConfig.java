package com.everbit.everbit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Upbit API 설정
 */
@Configuration
@ConfigurationProperties(prefix = "upbit.api")
@Getter
@Setter
public class UpbitConfig {
    
    /**
     * Upbit API 기본 URL
     */
    private String baseUrl;
    
    /**
     * Upbit API 액세스 키
     */
    private String accessKey;
    
    /**
     * Upbit API 시크릿 키
     */
    private String secretKey;
} 