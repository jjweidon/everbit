package com.everbit.everbit.user.domain;

import com.everbit.everbit.global.jpa.BaseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Kill Switch(계정/전략 실행 제어). SoT: docs/architecture/data-model.md §2.6.
 * 공유 PK: owner_id = PK = FK(app_user.id).
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KillSwitch extends BaseEntity {

	@Id
	@Column(name = "owner_id")
	private Long ownerId;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id")
	private AppUser owner;

	@Column(nullable = false)
	private boolean accountEnabled;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", nullable = false)
	private JsonNode enabledStrategies;

	@Builder(access = AccessLevel.PRIVATE)
	private KillSwitch(AppUser owner, boolean accountEnabled, JsonNode enabledStrategies) {
		this.owner = owner;
		this.ownerId = owner.getId();
		this.accountEnabled = accountEnabled;
		this.enabledStrategies = enabledStrategies;
	}

	public static KillSwitch init(AppUser owner) {
		return KillSwitch.builder()
			.owner(owner)
			.accountEnabled(true)
			.enabledStrategies(JsonNodeFactory.instance.arrayNode())
			.build();
	}

	public void enableAccount() {
		this.accountEnabled = true;
	}

	public void disableAccount() {
		this.accountEnabled = false;
	}
}
