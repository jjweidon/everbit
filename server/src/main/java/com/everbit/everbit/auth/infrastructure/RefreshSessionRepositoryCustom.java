package com.everbit.everbit.auth.infrastructure;

import java.time.Instant;
import java.util.Optional;

/**
 * RefreshSession QueryDSL 기반 커스텀 조회/삭제.
 */
public interface RefreshSessionRepositoryCustom {

	Optional<RefreshSession> findValidByJti(String jti, Instant now);

	long deleteByJti(String jti);
}
