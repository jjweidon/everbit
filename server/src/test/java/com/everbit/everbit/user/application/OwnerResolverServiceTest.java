package com.everbit.everbit.user.application;

import com.everbit.everbit.user.domain.AppUser;
import com.everbit.everbit.user.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

/**
 * OWNER 락 검증. SoT: docs/requirements/functional.md FR-AUTH-002.
 * Docker 필요. Docker 없으면 스킵.
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@Import(OwnerResolverService.class)
class OwnerResolverServiceTest {

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

	@Autowired
	OwnerResolverService ownerResolverService;

	@Autowired
	AppUserRepository appUserRepository;

	@Test
	void firstLogin_createsOwner() {
		AppUser owner = ownerResolverService.findOrCreateOwner("12345", "test@example.com");

		assertThat(owner.getKakaoId()).isEqualTo("12345");
		assertThat(owner.getEmail()).isEqualTo("test@example.com");
		assertThat(appUserRepository.count()).isEqualTo(1);
	}

	@Test
	void existingOwner_returnsSameUser() {
		AppUser first = ownerResolverService.findOrCreateOwner("12345", "test@example.com");
		AppUser second = ownerResolverService.findOrCreateOwner("12345", "test@example.com");

		assertThat(first.getId()).isEqualTo(second.getId());
		assertThat(appUserRepository.count()).isEqualTo(1);
	}

	@Test
	void differentKakaoId_afterOwnerExists_throwsNotOwner() {
		ownerResolverService.findOrCreateOwner("12345", "owner@example.com");

		assertThatThrownBy(() -> ownerResolverService.findOrCreateOwner("99999", "other@example.com"))
			.isInstanceOf(NotOwnerException.class);

		assertThat(appUserRepository.count()).isEqualTo(1);
	}
}
