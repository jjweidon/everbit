package com.everbit.everbit.strategy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * strategy_config 복합 PK. SoT: docs/architecture/jpa-mapping.md §4.2.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class StrategyConfigId implements Serializable {

	@Column(nullable = false)
	private Long ownerId;

	@Column(nullable = false, length = 64)
	private String strategyKey;

	public StrategyConfigId(Long ownerId, String strategyKey) {
		this.ownerId = ownerId;
		this.strategyKey = strategyKey;
	}
}
