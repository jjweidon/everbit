package com.everbit.everbit.user.application;

import com.everbit.everbit.user.domain.AppUser;

/**
 * OWNER 조회/생성 포트. auth 모듈에서 호출.
 * SoT: docs/integrations/kakao-oauth-auth-flow.md §7.5.
 */
public interface OwnerResolver {

	/**
	 * kakao_id로 기존 OWNER 조회 또는 최초 로그인 시 신규 생성.
	 *
	 * @param kakaoId 카카오 사용자 식별자
	 * @param email   카카오 이메일 (nullable)
	 * @return 기존 또는 신규 AppUser
	 * @throws NotOwnerException OWNER가 이미 존재하고, 요청한 kakao_id가 그 계정이 아닐 때
	 */
	AppUser findOrCreateOwner(String kakaoId, String email);
}
