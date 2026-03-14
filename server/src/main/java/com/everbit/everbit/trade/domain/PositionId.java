package com.everbit.everbit.trade.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * position 복합 PK. SoT: docs/architecture/jpa-mapping.md §4.4.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class PositionId implements Serializable {

	@Column(nullable = false)
	private Long ownerId;

	@Column(nullable = false, length = 32)
	private String market;

	public PositionId(Long ownerId, String market) {
		this.ownerId = ownerId;
		this.market = market;
	}
}
