package com.everbit.everbit.backtest.domain;

import com.everbit.everbit.global.jpa.BaseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 백테스트 결과(공유 PK). SoT: docs/architecture/data-model.md §5.2.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BacktestResult extends BaseEntity {

	@Id
	@Column(name = "job_id")
	private Long jobId;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "job_id")
	private BacktestJob job;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private JsonNode metricsJson;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private JsonNode equityCurveJson;

	@Builder(access = AccessLevel.PRIVATE)
	private BacktestResult(BacktestJob job, JsonNode metricsJson, JsonNode equityCurveJson) {
		this.job = job;
		this.jobId = job.getId();
		this.metricsJson = metricsJson;
		this.equityCurveJson = equityCurveJson;
	}

	public static BacktestResult create(BacktestJob job, JsonNode metricsJson, JsonNode equityCurveJson) {
		return BacktestResult.builder()
			.job(job)
			.metricsJson(metricsJson)
			.equityCurveJson(equityCurveJson)
			.build();
	}
}
