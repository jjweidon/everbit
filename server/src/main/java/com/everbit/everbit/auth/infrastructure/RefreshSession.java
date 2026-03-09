package com.everbit.everbit.auth.infrastructure;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.time.Instant;

/**
 * Refresh session JPA 엔티티. DB 저장용.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshSession {

	@Id
	@Column(length = 64, nullable = false)
	private String jti;

	@Column(nullable = false)
	private Long ownerId;

	@Column(nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private Instant createdAt;

	private RefreshSession(String jti, Long ownerId, Instant expiresAt) {
		this.jti = jti;
		this.ownerId = ownerId;
		this.expiresAt = expiresAt;
		this.createdAt = Instant.now();
	}

	@NonNull
	public static RefreshSession of(String jti, long ownerId, Instant expiresAt) {
		return new RefreshSession(jti, ownerId, expiresAt);
	}
}
