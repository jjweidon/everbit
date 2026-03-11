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
import java.util.UUID;

/**
 * Fill(체결). SoT: docs/architecture/data-model.md §2.11.
 * UNIQUE(trade_uuid) - trade_uuid가 멱등키.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"trade_uuid"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Fill extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "fill_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private AppUser owner;

	@Column(name = "upbit_uuid", nullable = false, columnDefinition = "uuid")
	private UUID upbitUuid;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumns({
		@JoinColumn(name = "upbit_uuid", referencedColumnName = "upbit_uuid", nullable = false, insertable = false, updatable = false),
		@JoinColumn(name = "owner_id", referencedColumnName = "owner_id", nullable = false, insertable = false, updatable = false)
	})
	private UpbitOrder upbitOrder;

	@Column(nullable = false, unique = true, columnDefinition = "uuid")
	private UUID tradeUuid;

	@Column(nullable = false)
	private Instant tradeTime;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal price;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal volume;

	@Column(precision = 38, scale = 18)
	private BigDecimal fee;

	@Builder(access = AccessLevel.PRIVATE)
	private Fill(AppUser owner, UpbitOrder upbitOrder, UUID tradeUuid, Instant tradeTime,
		BigDecimal price, BigDecimal volume, BigDecimal fee) {
		this.owner = owner;
		this.upbitOrder = upbitOrder;
		this.upbitUuid = upbitOrder.getUpbitUuid();
		this.tradeUuid = tradeUuid;
		this.tradeTime = tradeTime;
		this.price = price;
		this.volume = volume;
		this.fee = fee;
	}

	public static Fill create(AppUser owner, UpbitOrder upbitOrder, UUID tradeUuid, Instant tradeTime,
		BigDecimal price, BigDecimal volume, BigDecimal fee) {
		return Fill.builder()
			.owner(owner)
			.upbitOrder(upbitOrder)
			.tradeUuid(tradeUuid)
			.tradeTime(tradeTime)
			.price(price)
			.volume(volume)
			.fee(fee)
			.build();
	}
}
