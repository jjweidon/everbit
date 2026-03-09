package com.everbit.everbit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Spring context + JPA(ddl-auto) + Postgres 로드 검증. Postgres는 Testcontainers 사용.
 * Docker 필요. Docker 없이 stub 검증만 하려면 DashboardControllerTest만 실행.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Disabled("Requires Docker for Testcontainers. Run without this to pass stub tests only.")
class EverbitApplicationTests {

	@Container
	@SuppressWarnings("resource") // Testcontainers @Container manages lifecycle
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
		.withDatabaseName("everbit")
		.withUsername("everbit")
		.withPassword("everbit");

	@DynamicPropertySource
	static void datasourceProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Test
	void contextLoads() {
	}
}
