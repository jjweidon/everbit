package com.everbit.everbit.user.domain;

import com.everbit.everbit.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.Instant;

/**
 * Upbit API 키(암호문 저장). SoT: docs/architecture/data-model.md §2.2.
 * 공유 PK: owner_id = PK = FK(app_user.id).
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpbitKey extends BaseEntity {

	@Id
	@Column(name = "owner_id")
	private Long ownerId;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id")
	private AppUser owner;

	@Column(nullable = false)
	private byte[] accessKeyEnc;

	@Column(nullable = false)
	private byte[] secretKeyEnc;

	@Column(nullable = false)
	private int keyVersion;

	private Instant rotatedAt;

	@Builder(access = AccessLevel.PRIVATE)
	private UpbitKey(AppUser owner, byte[] accessKeyEnc, byte[] secretKeyEnc, int keyVersion) {
		this.owner = owner;
		this.ownerId = owner.getId();
		this.accessKeyEnc = accessKeyEnc;
		this.secretKeyEnc = secretKeyEnc;
		this.keyVersion = keyVersion;
	}

	public static UpbitKey create(AppUser owner, byte[] accessKeyEnc, byte[] secretKeyEnc, int keyVersion) {
		return UpbitKey.builder()
			.owner(owner)
			.accessKeyEnc(accessKeyEnc)
			.secretKeyEnc(secretKeyEnc)
			.keyVersion(keyVersion)
			.build();
	}

	public void rotate(byte[] newAccessKeyEnc, byte[] newSecretKeyEnc, int newVersion) {
		this.accessKeyEnc = newAccessKeyEnc;
		this.secretKeyEnc = newSecretKeyEnc;
		this.keyVersion = newVersion;
		this.rotatedAt = Instant.now();
	}
}
