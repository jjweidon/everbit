package com.everbit.everbit.notification.domain;

import com.everbit.everbit.global.jpa.BaseEntity;
import com.everbit.everbit.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Push 구독. SoT: docs/architecture/data-model.md §4.1.
 * UNIQUE(owner_id, endpoint)
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"owner_id", "endpoint"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PushSubscription extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "push_subscription_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private AppUser owner;

	@Column(nullable = false, columnDefinition = "text")
	private String endpoint;

	@Column(nullable = false, columnDefinition = "text")
	private String p256dh;

	@Column(nullable = false, columnDefinition = "text")
	private String auth;

	@Column(columnDefinition = "text")
	private String userAgent;

	@Column(nullable = false)
	private boolean enabled;

	@Builder(access = AccessLevel.PRIVATE)
	private PushSubscription(AppUser owner, String endpoint, String p256dh, String auth,
		String userAgent, boolean enabled) {
		this.owner = owner;
		this.endpoint = endpoint;
		this.p256dh = p256dh;
		this.auth = auth;
		this.userAgent = userAgent;
		this.enabled = enabled;
	}

	public static PushSubscription create(AppUser owner, String endpoint, String p256dh, String auth, String userAgent) {
		return PushSubscription.builder()
			.owner(owner)
			.endpoint(endpoint)
			.p256dh(p256dh)
			.auth(auth)
			.userAgent(userAgent)
			.enabled(true)
			.build();
	}

	public void disable() {
		this.enabled = false;
	}

	public void enable() {
		this.enabled = true;
	}
}
