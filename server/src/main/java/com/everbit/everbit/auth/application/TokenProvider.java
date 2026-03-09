package com.everbit.everbit.auth.application;

public interface TokenProvider {
	String createAccessToken(long ownerId);
	Long parseOwnerId(String token);
	int getAccessTokenTtlSeconds();
}
