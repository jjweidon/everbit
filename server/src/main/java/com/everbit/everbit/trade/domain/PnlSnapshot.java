package com.everbit.everbit.trade.domain;

import com.everbit.everbit.global.jpa.BaseEntity;
import com.everbit.everbit.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * PnL 스냅샷. SoT: docs/architecture/data-model.md §2.13.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PnlSnapshot extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pnl_snapshot_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private AppUser owner;

	@Column(nullable = false, length = 32)
	private String market;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal realizedPnl;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal unrealizedPnl;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal equity;

	@Column(nullable = false)
	private Instant capturedAt;

	@Builder(access = AccessLevel.PRIVATE)
	private PnlSnapshot(AppUser owner, String market, BigDecimal realizedPnl, BigDecimal unrealizedPnl,
		BigDecimal equity, Instant capturedAt) {
		this.owner = owner;
		this.market = market;
		this.realizedPnl = realizedPnl != null ? realizedPnl : BigDecimal.ZERO;
		this.unrealizedPnl = unrealizedPnl != null ? unrealizedPnl : BigDecimal.ZERO;
		this.equity = equity != null ? equity : BigDecimal.ZERO;
		this.capturedAt = capturedAt;
	}

	public static PnlSnapshot create(AppUser owner, String market, BigDecimal realizedPnl,
		BigDecimal unrealizedPnl, BigDecimal equity, Instant capturedAt) {
		return PnlSnapshot.builder()
			.owner(owner)
			.market(market)
			.realizedPnl(realizedPnl)
			.unrealizedPnl(unrealizedPnl)
			.equity(equity)
			.capturedAt(capturedAt)
			.build();
	}
}
