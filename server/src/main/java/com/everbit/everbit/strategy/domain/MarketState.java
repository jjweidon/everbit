package com.everbit.everbit.strategy.domain;

import com.everbit.everbit.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 마켓 실행 상태(ACTIVE/SUSPENDED). SoT: docs/architecture/data-model.md §2.5.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MarketState extends BaseEntity {

	@EmbeddedId
	private MarketStateId id;

	@Column(nullable = false, length = 16)
	private String tradeStatus;

	@Column(length = 64)
	private String suspendReasonCode;

	private Instant suspendedAt;

	private Instant lastSignalAt;

	private Instant cooldownUntil;

	@Builder(access = AccessLevel.PRIVATE)
	private MarketState(Long ownerId, String market, String tradeStatus) {
		this.id = new MarketStateId(ownerId, market);
		this.tradeStatus = tradeStatus;
	}

	public static MarketState createActive(Long ownerId, String market) {
		return MarketState.builder()
			.ownerId(ownerId)
			.market(market)
			.tradeStatus("ACTIVE")
			.build();
	}

	public void suspend(String reasonCode) {
		this.tradeStatus = "SUSPENDED";
		this.suspendReasonCode = reasonCode;
		this.suspendedAt = Instant.now();
	}

	public void activate() {
		this.tradeStatus = "ACTIVE";
		this.suspendReasonCode = null;
	}

	public void updateLastSignalAt(Instant lastSignalAt) {
		this.lastSignalAt = lastSignalAt;
	}

	public void setCooldownUntil(Instant cooldownUntil) {
		this.cooldownUntil = cooldownUntil;
	}
}
