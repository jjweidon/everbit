package com.everbit.everbit.auth.application;

import com.everbit.everbit.auth.application.port.RefreshSessionStore;
import com.everbit.everbit.auth.config.AuthProperties;
import com.everbit.everbit.user.application.OwnerResolver;
import com.everbit.everbit.user.domain.AppUser;
import com.everbit.everbit.user.application.NotOwnerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * 인증 유스케이스. OAuth2 성공 후 회원 매핑/토큰 발급, Refresh, 로그아웃.
 * SoT: docs/integrations/kakao-oauth-auth-flow.md.
 * OAuth 인가 코드 교환·state 검증은 Spring Security OAuth2 Client가 수행.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final AuthProperties authProperties;
	private final TokenProvider tokenProvider;
	private final RefreshSessionStore refreshSessionStore;
	private final OwnerResolver ownerResolver;

	/**
	 * OAuth2 인증 완료 후 회원 식별/생성 및 서비스 토큰 발급.
	 * (Spring Security가 code 교환·user info 조회 후 호출하는 쪽에서 사용)
	 */
	@Transactional
	public LoginResult handleOAuth2Success(String kakaoId, String email) {
		AppUser owner;
		try {
			owner = ownerResolver.findOrCreateOwner(kakaoId, email);
		} catch (NotOwnerException e) {
			log.info("LOGIN_REJECTED_NOT_OWNER kakao_id={}", maskKakaoId(kakaoId));
			throw e;
		}

		String accessToken = tokenProvider.createAccessToken(owner.getId());
		String refreshJti = UUID.randomUUID().toString();
		Instant refreshExpires = Instant.now().plusSeconds(authProperties.jwtRefreshTtlSeconds());
		refreshSessionStore.save(refreshJti, owner.getId(), refreshExpires);

		log.info("LOGIN_SUCCESS owner_id={}", owner.getId());

		return new LoginResult(accessToken, refreshJti, refreshExpires, owner.getId());
	}

	/**
	 * Refresh Token으로 Access 재발급. Rotation 적용.
	 */
	@Transactional
	public RefreshResult refresh(String refreshJti) {
		Long ownerId = refreshSessionStore.consumeAndGetOwnerId(refreshJti);
		if (ownerId == null) {
			// 이미 소비됨 = 재사용 탐지
			log.warn("REFRESH_REUSE_DETECTED jti={}", maskJti(refreshJti));
			throw new RefreshReuseDetectedException();
		}

		String newAccessToken = tokenProvider.createAccessToken(ownerId);
		String newRefreshJti = UUID.randomUUID().toString();
		Instant newRefreshExpires = Instant.now().plusSeconds(authProperties.jwtRefreshTtlSeconds());
		refreshSessionStore.save(newRefreshJti, ownerId, newRefreshExpires);

		log.info("REFRESH_SUCCESS owner_id={}", ownerId);

		return new RefreshResult(newAccessToken, newRefreshJti, newRefreshExpires);
	}

	/**
	 * 로그아웃. Refresh 세션 무효화.
	 */
	@Transactional
	public void logout(String refreshJti) {
		refreshSessionStore.invalidate(refreshJti);
		log.info("LOGOUT jti invalidated");
	}

	private static String maskKakaoId(String kakaoId) {
		if (kakaoId == null || kakaoId.length() < 4) return "***";
		return kakaoId.substring(0, 2) + "***" + kakaoId.substring(kakaoId.length() - 2);
	}

	private static String maskJti(String jti) {
		if (jti == null || jti.length() < 8) return "***";
		return jti.substring(0, 4) + "***";
	}

	public record LoginResult(String accessToken, String refreshJti, Instant refreshExpiresAt, long ownerId) {}
	public record RefreshResult(String accessToken, String refreshJti, Instant refreshExpiresAt) {}

	public static class RefreshReuseDetectedException extends RuntimeException {
		public RefreshReuseDetectedException() {
			super("세션이 만료되었습니다. 다시 로그인해 주세요.");
		}
	}
}
