package com.everbit.everbit.backtest.domain;

import com.everbit.everbit.global.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 캔들 캐시(백테스트 canonical source). SoT: docs/architecture/data-model.md §5.0.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CandleCache extends BaseEntity {

	@EmbeddedId
	private CandleCacheId id;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal open;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal high;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal low;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal close;

	@Column(nullable = false, precision = 38, scale = 18)
	private BigDecimal volume;

	@Builder(access = AccessLevel.PRIVATE)
	private CandleCache(CandleCacheId id, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, BigDecimal volume) {
		this.id = id;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume != null ? volume : BigDecimal.ZERO;
	}

	public static CandleCache create(String market, String timeframe, java.time.Instant candleTime,
		BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, BigDecimal volume) {
		CandleCacheId id = new CandleCacheId(market, timeframe, candleTime);
		return CandleCache.builder()
			.id(id)
			.open(open)
			.high(high)
			.low(low)
			.close(close)
			.volume(volume)
			.build();
	}
}
