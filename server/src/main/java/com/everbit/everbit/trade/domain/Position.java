package com.everbit.everbit.trade.domain;

import com.everbit.everbit.global.jpa.BaseEntity;
import com.everbit.everbit.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Position(마켓별 포지션). SoT: docs/architecture/data-model.md §2.12.
 * status: FLAT/OPEN (보유 상태). 실행 중단은 market_state.trade_status.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Position extends BaseEntity {

	@EmbeddedId
	private PositionId id;

	@MapsId("ownerId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private AppUser owner;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal quantity;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal avgPrice;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private PositionStatus status;

	@Builder(access = AccessLevel.PRIVATE)
	private Position(AppUser owner, String market, BigDecimal quantity, BigDecimal avgPrice, PositionStatus status) {
		this.owner = owner;
		this.id = new PositionId(owner.getId(), market);
		this.quantity = quantity;
		this.avgPrice = avgPrice;
		this.status = status;
	}

	public static Position flat(AppUser owner, String market) {
		return Position.builder()
			.owner(owner)
			.market(market)
			.quantity(BigDecimal.ZERO)
			.avgPrice(BigDecimal.ZERO)
			.status(PositionStatus.FLAT)
			.build();
	}

	public static Position open(AppUser owner, String market, BigDecimal quantity, BigDecimal avgPrice) {
		return Position.builder()
			.owner(owner)
			.market(market)
			.quantity(quantity)
			.avgPrice(avgPrice)
			.status(PositionStatus.OPEN)
			.build();
	}

	public void updatePosition(BigDecimal quantity, BigDecimal avgPrice, PositionStatus status) {
		this.quantity = quantity;
		this.avgPrice = avgPrice;
		this.status = status;
	}
}
