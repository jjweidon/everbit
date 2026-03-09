package com.everbit.everbit.auth.infrastructure;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

import static com.everbit.everbit.auth.infrastructure.QRefreshSession.refreshSession;

/**
 * QueryDSL 기반 RefreshSession 조회/삭제 구현.
 * Spring Data JPA 커스텀 fragment 규칙: 인터페이스명이 *Custom 이면 구현체는 *CustomImpl.
 * SoT: docs/architecture/spring-boot-conventions.md §8.1.
 */
@Repository
@RequiredArgsConstructor
public class RefreshSessionRepositoryCustomImpl implements RefreshSessionRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Optional<RefreshSession> findValidByJti(String jti, Instant now) {
		RefreshSession result = queryFactory
			.selectFrom(refreshSession)
			.where(
				refreshSession.jti.eq(jti),
				refreshSession.expiresAt.after(now)
			)
			.fetchOne();
		return Optional.ofNullable(result);
	}

	@Override
	public long deleteByJti(String jti) {
		return queryFactory
			.delete(refreshSession)
			.where(refreshSession.jti.eq(jti))
			.execute();
	}
}
