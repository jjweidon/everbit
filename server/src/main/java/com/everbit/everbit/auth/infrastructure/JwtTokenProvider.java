package com.everbit.everbit.auth.infrastructure;

import com.everbit.everbit.auth.application.TokenProvider;
import com.everbit.everbit.auth.config.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider implements TokenProvider {

	private final AuthProperties authProperties;

	@Override
	public String createAccessToken(long ownerId) {
		SecretKey key = Keys.hmacShaKeyFor(authProperties.jwtAccessSecret().getBytes(StandardCharsets.UTF_8));
		Date expiresAt = Date.from(
			java.time.Instant.now().plusSeconds(authProperties.jwtAccessTtlSeconds())
		);
		return Jwts.builder()
			.subject(String.valueOf(ownerId))
			.expiration(expiresAt)
			.signWith(key)
			.compact();
	}

	@Override
	public Long parseOwnerId(String token) {
		try {
			SecretKey key = Keys.hmacShaKeyFor(authProperties.jwtAccessSecret().getBytes(StandardCharsets.UTF_8));
			Claims claims = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
			return Long.parseLong(claims.getSubject());
		} catch (ExpiredJwtException | io.jsonwebtoken.security.SignatureException | NumberFormatException e) {
			return null;
		}
	}

	@Override
	public int getAccessTokenTtlSeconds() {
		return authProperties.jwtAccessTtlSeconds();
	}
}
