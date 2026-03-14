package com.everbit.everbit.trade.domain;

import com.everbit.everbit.global.jpa.BaseEntity;
import com.everbit.everbit.global.util.Uuids;
import com.everbit.everbit.user.domain.AppUser;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Signal(멱등 시작점). SoT: docs/architecture/data-model.md §2.7, order-pipeline.md.
 * UNIQUE(owner_id, strategy_key, market, timeframe, candle_close_time, side)
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(
	columnNames = {"owner_id", "strategy_key", "market", "timeframe", "candle_close_time", "side"}
))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Signal extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "signal_id")
	private Long id;

	@Column(nullable = false, unique = true, columnDefinition = "uuid")
	private UUID publicId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private AppUser owner;

	@Column(name = "owner_id", nullable = false, insertable = false, updatable = false)
	private Long ownerId;

	@Column(nullable = false, length = 64)
	private String strategyKey;

	@Column(nullable = false, length = 32)
	private String market;

	@Column(nullable = false, length = 16)
	private String timeframe;

	@Column(nullable = false)
	private Instant candleCloseTime;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 8)
	private SignalSide side;

	@Column(nullable = false, precision = 18, scale = 8)
	private BigDecimal strength;

	@Column(length = 64)
	private String reasonCode;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private JsonNode signalJson;

	@Builder(access = AccessLevel.PRIVATE)
	private Signal(UUID publicId, AppUser owner, String strategyKey, String market, String timeframe,
		Instant candleCloseTime, SignalSide side, BigDecimal strength, String reasonCode, JsonNode signalJson) {
		this.publicId = publicId;
		this.owner = owner;
		this.strategyKey = strategyKey;
		this.market = market;
		this.timeframe = timeframe;
		this.candleCloseTime = candleCloseTime;
		this.side = side;
		this.strength = strength;
		this.reasonCode = reasonCode;
		this.signalJson = signalJson;
	}

	public static Signal create(AppUser owner, String strategyKey, String market, String timeframe,
		Instant candleCloseTime, SignalSide side, BigDecimal strength, String reasonCode, JsonNode signalJson) {
		return Signal.builder()
			.publicId(Uuids.next())
			.owner(owner)
			.strategyKey(strategyKey)
			.market(market)
			.timeframe(timeframe)
			.candleCloseTime(candleCloseTime)
			.side(side)
			.strength(strength)
			.reasonCode(reasonCode)
			.signalJson(signalJson)
			.build();
	}
}
