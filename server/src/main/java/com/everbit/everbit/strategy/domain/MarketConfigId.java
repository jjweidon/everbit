package com.everbit.everbit.strategy.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * market_config 복합 PK. SoT: docs/architecture/jpa-mapping.md §4.4.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class MarketConfigId implements Serializable {

	@Column(nullable = false)
	private Long ownerId;

	@Column(nullable = false, length = 32)
	private String market;

	public MarketConfigId(Long ownerId, String market) {
		this.ownerId = ownerId;
		this.market = market;
	}
}
