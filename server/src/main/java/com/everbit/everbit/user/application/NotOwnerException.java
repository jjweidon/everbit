package com.everbit.everbit.user.application;

/**
 * OWNER가 이미 존재할 때 다른 카카오 계정 로그인 시도.
 * SoT: docs/integrations/kakao-oauth-auth-flow.md §6.4.
 */
public class NotOwnerException extends RuntimeException {

	public NotOwnerException() {
		super("이 서비스는 등록된 계정만 사용할 수 있습니다.");
	}
}
