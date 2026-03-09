package com.everbit.everbit.auth.application.port;

import java.time.Instant;

/**
 * Refresh Token(jti) 저장소 포트.
 * SoT: docs/adr/0007-auth-session.md, docs/integrations/kakao-oauth-auth-flow.md.
 * 로컬: InMemory, 운영: DB 구현 사용(Redis 컨테이너 전제하지 않음).
 */
public interface RefreshSessionStore {

	/**
	 * Refresh 세션 저장.
	 *
	 * @param jti      Refresh Token 고유 식별자
	 * @param ownerId  app_user.id
	 * @param expiresAt 만료 시각
	 */
	void save(String jti, long ownerId, Instant expiresAt);

	/**
	 * jti로 owner_id 조회 후 세션 무효화(삭제).
	 * 재사용 탐지 시 호출.
	 *
	 * @param jti Refresh Token jti
	 * @return owner_id (존재 시), 없으면 null
	 */
	Long consumeAndGetOwnerId(String jti);

	/**
	 * jti로 owner_id 조회. consume하지 않음.
	 *
	 * @param jti Refresh Token jti
	 * @return owner_id (존재·미만료 시), 없으면 null
	 */
	Long findOwnerId(String jti);

	/**
	 * jti 무효화(로그아웃 등).
	 */
	void invalidate(String jti);
}
