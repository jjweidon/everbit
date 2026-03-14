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

import java.time.Instant;
import java.util.UUID;

/**
 * OrderAttempt(Upbit 호출 1회). SoT: docs/architecture/data-model.md §2.9.
 * UNIQUE(order_intent_id, attempt_no), UNIQUE(identifier)
 */
@Entity
@Table(uniqueConstraints = {
	@UniqueConstraint(columnNames = {"order_intent_id", "attempt_no"}),
	@UniqueConstraint(columnNames = {"identifier"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderAttempt extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_attempt_id")
	private Long id;

	@Column(nullable = false, unique = true, columnDefinition = "uuid")
	private UUID publicId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private AppUser owner;

	@Column(name = "order_intent_id", nullable = false)
	private Long orderIntentId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumns({
		@JoinColumn(name = "order_intent_id", referencedColumnName = "order_intent_id", nullable = false, insertable = false, updatable = false),
		@JoinColumn(name = "owner_id", referencedColumnName = "owner_id", nullable = false, insertable = false, updatable = false)
	})
	private OrderIntent orderIntent;

	@Column(nullable = false)
	private int attemptNo;

	@Column(nullable = false, length = 64)
	private String identifier;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private JsonNode requestJson;

	@Column(columnDefinition = "uuid")
	private UUID upbitUuid;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private OrderAttemptStatus status;

	@Column(length = 64)
	private String errorCode;

	@Column(columnDefinition = "text")
	private String errorMessage;

	private Instant nextRetryAt;

	@Builder(access = AccessLevel.PRIVATE)
	private OrderAttempt(UUID publicId, AppUser owner, OrderIntent orderIntent, int attemptNo, String identifier,
		JsonNode requestJson, OrderAttemptStatus status) {
		this.publicId = publicId;
		this.owner = owner;
		this.orderIntent = orderIntent;
		this.orderIntentId = orderIntent.getId();
		this.attemptNo = attemptNo;
		this.identifier = identifier;
		this.requestJson = requestJson;
		this.status = status;
	}

	public static OrderAttempt create(AppUser owner, OrderIntent orderIntent, int attemptNo,
		JsonNode requestJson) {
		return OrderAttempt.builder()
			.publicId(Uuids.next())
			.owner(owner)
			.orderIntent(orderIntent)
			.attemptNo(attemptNo)
			.identifier(Uuids.next().toString())
			.requestJson(requestJson)
			.status(OrderAttemptStatus.PREPARED)
			.build();
	}

	public void markSent() {
		this.status = OrderAttemptStatus.SENT;
	}

	public void markAcked(UUID upbitUuid) {
		this.status = OrderAttemptStatus.ACKED;
		this.upbitUuid = upbitUuid;
	}

	public void markRejected(String errorCode, String errorMessage) {
		this.status = OrderAttemptStatus.REJECTED;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public void markThrottled(Instant nextRetryAt) {
		this.status = OrderAttemptStatus.THROTTLED;
		this.nextRetryAt = nextRetryAt;
	}

	public void markUnknown() {
		this.status = OrderAttemptStatus.UNKNOWN;
	}

	public void markSuspended() {
		this.status = OrderAttemptStatus.SUSPENDED;
	}
}
