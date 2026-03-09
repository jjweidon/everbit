package com.everbit.everbit.auth.application.port;

import java.time.Instant;

/**
 * OAuth state 파라미터 저장소. CSRF 방지용.
 * SoT: docs/integrations/kakao-oauth-auth-flow.md §9.1.
 */
public interface OAuthStateStore {

	/**
	 * state 저장. TTL 10분.
	 */
	void save(String state, Instant expiresAt);

	/**
	 * state 검증 후 소비(삭제).
	 *
	 * @return 유효 시 true, 없거나 만료 시 false
	 */
	boolean consume(String state);
}
