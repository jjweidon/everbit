package com.everbit.everbit.global.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정. EntityManagerFactory가 있을 때만 로드되어
 * @WebMvcTest 등 slice 테스트에서 JPA metamodel 오류를 방지.
 */
@Configuration
@ConditionalOnBean(EntityManagerFactory.class)
@EnableJpaAuditing
public class JpaAuditingConfig {
}
