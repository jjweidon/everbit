package com.everbit.everbit.global.config;

import com.everbit.everbit.global.crypto.UpbitKeyCryptoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(UpbitKeyCryptoProperties.class)
public class GlobalCryptoConfig {
}
