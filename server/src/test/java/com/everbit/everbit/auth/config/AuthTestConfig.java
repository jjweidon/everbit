package com.everbit.everbit.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;

/**
 * Auth 테스트용 설정. application-test.yml의 auth.* 값을 참조.
 * SoT: docs/testing/backend-tdd-template.md §10.
 */
@TestConfiguration
@EnableConfigurationProperties(AuthProperties.class)
public class AuthTestConfig {
}
