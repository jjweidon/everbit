package com.everbit.everbit.trade.domain;

import com.everbit.everbit.global.jpa.BaseEntity;
import com.everbit.everbit.global.util.Uuids;
import com.everbit.everbit.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * OrderIntent(주문 의도). SoT: docs/architecture/data-model.md §2.8.
 * UNIQUE(signal_id, intent_type)
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"signal_id", "intent_type"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderIntent extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_intent_id")
	private Long id;

	@Column(nullable = false, unique = true, columnDefinition = "uuid")
	private UUID publicId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private AppUser owner;

	@Column(name = "owner_id", nullable = false, insertable = false, updatable = false)
	private Long ownerId;

	@Column(name = "signal_id", nullable = false)
	private Long signalId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumns({
		@JoinColumn(name = "signal_id", referencedColumnName = "signal_id", nullable = false, insertable = false, updatable = false),
		@JoinColumn(name = "owner_id", referencedColumnName = "owner_id", nullable = false, insertable = false, updatable = false)
	})
	private Signal signal;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private OrderIntentType intentType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private OrderIntentStatus status;

	@Column(nullable = false, length = 32)
	private String market;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 8)
	private SignalSide side;

	@Column(precision = 38, scale = 18)
	private BigDecimal requestedKrw;

	@Column(precision = 38, scale = 18)
	private BigDecimal requestedVolume;

	@Column(length = 64)
	private String reasonCode;

	@Builder(access = AccessLevel.PRIVATE)
	private OrderIntent(UUID publicId, AppUser owner, Signal signal, OrderIntentType intentType, OrderIntentStatus status,
		String market, SignalSide side, BigDecimal requestedKrw, BigDecimal requestedVolume, String reasonCode) {
		this.publicId = publicId;
		this.owner = owner;
		this.signal = signal;
		this.signalId = signal.getId();
		this.intentType = intentType;
		this.status = status;
		this.market = market;
		this.side = side;
		this.requestedKrw = requestedKrw;
		this.requestedVolume = requestedVolume;
		this.reasonCode = reasonCode;
	}

	public static OrderIntent createEntry(AppUser owner, Signal signal, String market, BigDecimal requestedKrw) {
		return OrderIntent.builder()
			.publicId(Uuids.next())
			.owner(owner)
			.signal(signal)
			.intentType(OrderIntentType.ENTRY)
			.status(OrderIntentStatus.CREATED)
			.market(market)
			.side(SignalSide.BUY)
			.requestedKrw(requestedKrw)
			.build();
	}

	public static OrderIntent createExit(AppUser owner, Signal signal, OrderIntentType exitType,
		String market, BigDecimal requestedVolume, String reasonCode) {
		return OrderIntent.builder()
			.publicId(Uuids.next())
			.owner(owner)
			.signal(signal)
			.intentType(exitType)
			.status(OrderIntentStatus.CREATED)
			.market(market)
			.side(SignalSide.SELL)
			.requestedVolume(requestedVolume)
			.reasonCode(reasonCode)
			.build();
	}

	public void complete() {
		this.status = OrderIntentStatus.COMPLETED;
	}

	public void cancel() {
		this.status = OrderIntentStatus.CANCELED;
	}
}
