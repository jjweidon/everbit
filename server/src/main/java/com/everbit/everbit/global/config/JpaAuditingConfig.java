package com.everbit.everbit.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정. created_at/updated_at 자동 주입.
 * @WebMvcTest 슬라이스 테스트에서 오류가 발생하면 해당 테스트에
 * @Import(JpaAuditingConfig.class) 제외 또는 MockBean 처리할 것.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
