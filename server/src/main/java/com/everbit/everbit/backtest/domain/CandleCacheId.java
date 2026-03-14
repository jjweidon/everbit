package com.everbit.everbit.backtest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * candle_cache 복합 PK. SoT: docs/architecture/data-model.md §5.0.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class CandleCacheId implements Serializable {

	@Column(nullable = false, length = 32)
	private String market;

	@Column(nullable = false, length = 16)
	private String timeframe;

	@Column(nullable = false)
	private Instant candleTime;

	public CandleCacheId(String market, String timeframe, Instant candleTime) {
		this.market = market;
		this.timeframe = timeframe;
		this.candleTime = candleTime;
	}
}
