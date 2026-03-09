package com.everbit.everbit.auth.infrastructure;

import com.everbit.everbit.auth.application.port.OAuthStateStore;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory OAuth state 저장소. 단일 인스턴스 전제.
 */
@Component
public class InMemoryOAuthStateStore implements OAuthStateStore {

	private final Map<String, Instant> store = new ConcurrentHashMap<>();

	@Override
	public void save(String state, Instant expiresAt) {
		store.put(state, expiresAt);
	}

	@Override
	public boolean consume(String state) {
		Instant expiresAt = store.remove(state);
		if (expiresAt == null) {
			return false;
		}
		return Instant.now().isBefore(expiresAt);
	}
}
