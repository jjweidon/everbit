package com.everbit.everbit.user.application;

/**
 * owner_id로 조회한 OWNER(AppUser)가 없을 때.
 * API: 404 NOT_FOUND, reasonCode OWNER_NOT_FOUND.
 */
public class OwnerNotFoundException extends RuntimeException {

	public OwnerNotFoundException(Long ownerId) {
		super("Owner not found: " + ownerId);
	}

	public OwnerNotFoundException(String message) {
		super(message);
	}
}
