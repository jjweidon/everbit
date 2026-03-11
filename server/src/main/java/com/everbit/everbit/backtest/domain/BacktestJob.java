package com.everbit.everbit.backtest.domain;

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

import java.util.UUID;

/**
 * 백테스트 Job. SoT: docs/architecture/data-model.md §5.1.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BacktestJob extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "backtest_job_id")
	private Long id;

	@Column(nullable = false, unique = true, columnDefinition = "uuid")
	private UUID publicId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private AppUser owner;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private BacktestJobStatus status;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private JsonNode requestJson;

	@Builder(access = AccessLevel.PRIVATE)
	private BacktestJob(UUID publicId, AppUser owner, BacktestJobStatus status, JsonNode requestJson) {
		this.publicId = publicId;
		this.owner = owner;
		this.status = status;
		this.requestJson = requestJson;
	}

	public static BacktestJob create(AppUser owner, JsonNode requestJson) {
		return BacktestJob.builder()
			.publicId(Uuids.next())
			.owner(owner)
			.status(BacktestJobStatus.QUEUED)
			.requestJson(requestJson)
			.build();
	}

	public void markRunning() {
		this.status = BacktestJobStatus.RUNNING;
	}

	public void markDone() {
		this.status = BacktestJobStatus.DONE;
	}

	public void markFailed() {
		this.status = BacktestJobStatus.FAILED;
	}
}
