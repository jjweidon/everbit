package com.everbit.everbit.oauth2.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(OAuthProperties.class)
public class OAuthConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 