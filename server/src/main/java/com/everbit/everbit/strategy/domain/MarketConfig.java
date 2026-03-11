package com.everbit.everbit.strategy.domain;

import com.everbit.everbit.global.jpa.BaseEntity;
import com.everbit.everbit.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 마켓 설정(Enable/Disable). SoT: docs/architecture/data-model.md §2.4.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketConfig extends BaseEntity {

	@EmbeddedId
	private MarketConfigId id;

	@MapsId("ownerId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private AppUser owner;

	@Column(nullable = false)
	private boolean enabled;

	@Column(nullable = false)
	private int priority;

	@Builder(access = AccessLevel.PRIVATE)
	private MarketConfig(AppUser owner, String market, boolean enabled, int priority) {
		this.owner = owner;
		this.id = new MarketConfigId(owner.getId(), market);
		this.enabled = enabled;
		this.priority = priority;
	}

	public static MarketConfig create(AppUser owner, String market) {
		return MarketConfig.builder()
			.owner(owner)
			.market(market)
			.enabled(true)
			.priority(0)
			.build();
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
}
