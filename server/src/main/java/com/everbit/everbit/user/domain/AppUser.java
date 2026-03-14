package com.everbit.everbit.user.domain;

import com.everbit.everbit.global.jpa.BaseEntity;
import com.everbit.everbit.global.util.Uuids;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 서비스 사용자(OWNER). SoT: docs/architecture/data-model.md §2.1.
 * v2 MVP: 1인 전용, kakao_id로 식별.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUser extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "owner_id")
	private Long id;

	@Column(nullable = false, unique = true, columnDefinition = "uuid")
	private UUID publicId;

	@Column(nullable = false, unique = true, length = 64)
	private String kakaoId;

	@Column
	private String email;

	@Builder(access = AccessLevel.PRIVATE)
	private AppUser(UUID publicId, String kakaoId, String email) {
		this.publicId = publicId;
		this.kakaoId = kakaoId;
		this.email = email;
	}

	public static AppUser create(String kakaoId, String email) {
		return AppUser.builder()
			.publicId(Uuids.next())
			.kakaoId(kakaoId)
			.email(email)
			.build();
	}

	public void changeEmail(String email) {
		this.email = email;
	}
}
