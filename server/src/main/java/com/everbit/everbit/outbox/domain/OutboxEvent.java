package com.everbit.everbit.outbox.domain;

import com.everbit.everbit.global.jpa.BaseEntity;
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
 * Outbox 이벤트. SoT: docs/architecture/event-bus.md, data-model.md §3.1.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "outbox_event_id")
	private Long id;

	@Column(nullable = false, unique = true, columnDefinition = "uuid")
	private UUID eventId;

	@Column(nullable = false, length = 128)
	private String stream;

	@Column(nullable = false, length = 128)
	private String eventType;

	@Column(nullable = false, length = 64)
	private String aggregateType;

	private Long aggregateId;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private JsonNode payloadJson;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private OutboxEventStatus status;

	@Column(nullable = false)
	private int attemptCount;

	@Column(nullable = false)
	private int maxAttempts;

	@Column(nullable = false)
	private Instant nextRetryAt;

	private Instant lastAttemptAt;

	@Column(length = 128)
	private String lockedBy;

	private Instant lockedUntil;

	@Column(length = 64)
	private String lastErrorCode;

	@Column(columnDefinition = "text")
	private String lastErrorMessage;

	private Instant processedAt;

	@Builder(access = AccessLevel.PRIVATE)
	private OutboxEvent(UUID eventId, String stream, String eventType, String aggregateType,
		Long aggregateId, JsonNode payloadJson, int maxAttempts) {
		this.eventId = eventId != null ? eventId : UUID.randomUUID();
		this.stream = stream;
		this.eventType = eventType;
		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
		this.payloadJson = payloadJson;
		this.status = OutboxEventStatus.PENDING;
		this.attemptCount = 0;
		this.maxAttempts = maxAttempts;
		this.nextRetryAt = Instant.now();
	}

	public static OutboxEvent create(String stream, String eventType, String aggregateType,
		Long aggregateId, JsonNode payloadJson) {
		return OutboxEvent.builder()
			.eventId(UUID.randomUUID())
			.stream(stream)
			.eventType(eventType)
			.aggregateType(aggregateType)
			.aggregateId(aggregateId)
			.payloadJson(payloadJson)
			.maxAttempts(10)
			.build();
	}

	public static OutboxEvent create(UUID eventId, String stream, String eventType, String aggregateType,
		Long aggregateId, JsonNode payloadJson, int maxAttempts) {
		return OutboxEvent.builder()
			.eventId(eventId)
			.stream(stream)
			.eventType(eventType)
			.aggregateType(aggregateType)
			.aggregateId(aggregateId)
			.payloadJson(payloadJson)
			.maxAttempts(maxAttempts)
			.build();
	}
}
