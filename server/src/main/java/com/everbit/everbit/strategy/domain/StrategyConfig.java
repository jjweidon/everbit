package com.everbit.everbit.strategy.domain;

import com.everbit.everbit.global.jpa.BaseEntity;
import com.everbit.everbit.user.domain.AppUser;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 전략 설정. SoT: docs/architecture/data-model.md §2.3.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StrategyConfig extends BaseEntity {

	@EmbeddedId
	private StrategyConfigId id;

	@MapsId("ownerId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private AppUser owner;

	@Column(nullable = false)
	private int configVersion;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb", nullable = false)
	private JsonNode configJson;

	@Builder(access = AccessLevel.PRIVATE)
	private StrategyConfig(AppUser owner, String strategyKey, int configVersion, JsonNode configJson) {
		this.owner = owner;
		this.id = new StrategyConfigId(owner.getId(), strategyKey);
		this.configVersion = configVersion;
		this.configJson = configJson;
	}

	public static StrategyConfig create(AppUser owner, String strategyKey, JsonNode configJson) {
		return StrategyConfig.builder()
			.owner(owner)
			.strategyKey(strategyKey)
			.configVersion(1)
			.configJson(configJson)
			.build();
	}

	public void update(JsonNode configJson) {
		this.configJson = configJson;
		this.configVersion++;
	}
}
