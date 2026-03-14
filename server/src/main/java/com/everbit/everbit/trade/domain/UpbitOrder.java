package com.everbit.everbit.trade.domain;

import com.everbit.everbit.global.jpa.BaseEntity;
import com.everbit.everbit.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Upbit 주문 실체. SoT: docs/architecture/data-model.md §2.10.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpbitOrder extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "upbit_order_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private AppUser owner;

	@Column(name = "owner_id", nullable = false, insertable = false, updatable = false)
	private Long ownerId;

	@Column(name = "upbit_uuid", nullable = false, unique = true, columnDefinition = "uuid")
	private UUID upbitUuid;

	@Column(length = 64)
	private String identifier;

	@Column(nullable = false, length = 32)
	private String market;

	@Column(nullable = false, length = 8)
	private String side;

	@Column(nullable = false, length = 16)
	private String ordType;

	@Column(nullable = false, length = 16)
	private String state;

	@Column(precision = 38, scale = 18)
	private BigDecimal price;

	@Column(precision = 38, scale = 18)
	private BigDecimal volume;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal executedVolume;

	@Builder(access = AccessLevel.PRIVATE)
	private UpbitOrder(AppUser owner, UUID upbitUuid, String identifier, String market,
		String side, String ordType, String state, BigDecimal price, BigDecimal volume, BigDecimal executedVolume) {
		this.owner = owner;
		this.upbitUuid = upbitUuid;
		this.identifier = identifier;
		this.market = market;
		this.side = side;
		this.ordType = ordType;
		this.state = state;
		this.price = price;
		this.volume = volume;
		this.executedVolume = executedVolume != null ? executedVolume : BigDecimal.ZERO;
	}

	public static UpbitOrder create(AppUser owner, UUID upbitUuid, String identifier, String market,
		String side, String ordType, String state, BigDecimal price, BigDecimal volume) {
		return UpbitOrder.builder()
			.owner(owner)
			.upbitUuid(upbitUuid)
			.identifier(identifier)
			.market(market)
			.side(side)
			.ordType(ordType)
			.state(state)
			.price(price)
			.volume(volume)
			.executedVolume(BigDecimal.ZERO)
			.build();
	}

	public void updateState(String state, BigDecimal executedVolume) {
		this.state = state;
		if (executedVolume != null) {
			this.executedVolume = executedVolume;
		}
	}
}
