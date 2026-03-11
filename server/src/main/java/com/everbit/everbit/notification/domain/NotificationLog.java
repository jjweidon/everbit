package com.everbit.everbit.notification.domain;

import com.everbit.everbit.global.jpa.BaseEntity;
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
 * Notification 로그(dedupe). SoT: docs/architecture/data-model.md §4.2.
 * UNIQUE(owner_id, event_id)
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "event_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationLog extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "notification_log_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private AppUser owner;

	@Column(nullable = false, columnDefinition = "uuid")
	private UUID eventId;

	@Column(nullable = false, length = 64)
	private String type;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private JsonNode payloadJson;

	private Instant deliveredAt;

	@Builder(access = AccessLevel.PRIVATE)
	private NotificationLog(AppUser owner, UUID eventId, String type, JsonNode payloadJson) {
		this.owner = owner;
		this.eventId = eventId;
		this.type = type;
		this.payloadJson = payloadJson;
	}

	public static NotificationLog create(AppUser owner, UUID eventId, String type, JsonNode payloadJson) {
		return NotificationLog.builder()
			.owner(owner)
			.eventId(eventId)
			.type(type)
			.payloadJson(payloadJson)
			.build();
	}

	public void markDelivered() {
		this.deliveredAt = Instant.now();
	}
}
