package com.everbit.everbit.auth.infrastructure;

import com.everbit.everbit.auth.application.port.RefreshSessionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * DB 기반 Refresh 세션 저장소. 운영 환경(Redis 없음)용.
 * SoT: docs/integrations/kakao-oauth-auth-flow.md §11.3.
 */
@Component
@RequiredArgsConstructor
public class DatabaseRefreshSessionStore implements RefreshSessionStore {

	private final RefreshSessionJpaRepository repository;

	@Override
	@Transactional
	public void save(String jti, long ownerId, Instant expiresAt) {
		repository.save(RefreshSession.of(jti, ownerId, expiresAt));
	}

	@Override
	@Transactional
	public Long consumeAndGetOwnerId(String jti) {
		Optional<RefreshSession> session = repository.findValidByJti(jti, Instant.now());
		if (session.isEmpty()) {
			return null;
		}
		Long ownerId = session.get().getOwnerId();
		repository.deleteByJti(jti);
		return ownerId;
	}

	@Override
	@Transactional(readOnly = true)
	public Long findOwnerId(String jti) {
		return repository.findValidByJti(jti, Instant.now())
			.map(RefreshSession::getOwnerId)
			.orElse(null);
	}

	@Override
	@Transactional
	public void invalidate(String jti) {
		repository.deleteByJti(jti);
	}
}
