package com.everbit.everbit.user.application;

/**
 * OWNER가 이미 존재할 때 다른 카카오 계정 로그인 시도.
 * SoT: docs/integrations/kakao-oauth-auth-flow.md §6.4.
 * API: 403 FORBIDDEN, reasonCode NOT_OWNER.
 */
public class NotOwnerException extends RuntimeException {

	private static final String DEFAULT_MESSAGE = "이 서비스는 등록된 계정만 사용할 수 있습니다.";

	public NotOwnerException() {
		super(DEFAULT_MESSAGE);
	}

	public NotOwnerException(String message) {
		super(message != null && !message.isBlank() ? message : DEFAULT_MESSAGE);
	}
}
