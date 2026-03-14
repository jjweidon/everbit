package com.everbit.everbit.user.domain;

import com.everbit.everbit.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.springframework.data.domain.Persistable;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.Objects;

/**
 * Upbit API 키(암호문 저장). SoT: docs/architecture/data-model.md §2.2.
 * 공유 PK: owner_id = PK = FK(app_user.id).
 * 새 엔티티일 때 save()가 merge 대신 persist를 쓰도록 Persistable 구현.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpbitKey extends BaseEntity implements Persistable<Long> {

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

	@Override
	public Long getId() {
		return ownerId;
	}

	@Override
	public boolean isNew() {
		return getCreatedAt() == null;
	}

	@NonNull
	public static UpbitKey create(AppUser owner, byte[] accessKeyEnc, byte[] secretKeyEnc, int keyVersion) {
		return Objects.requireNonNull(UpbitKey.builder()
			.owner(owner)
			.accessKeyEnc(accessKeyEnc)
			.secretKeyEnc(secretKeyEnc)
			.keyVersion(keyVersion)
			.build());
	}

	public void rotate(byte[] newAccessKeyEnc, byte[] newSecretKeyEnc, int newVersion) {
		this.accessKeyEnc = newAccessKeyEnc;
		this.secretKeyEnc = newSecretKeyEnc;
		this.keyVersion = newVersion;
		this.rotatedAt = Instant.now();
	}
}
