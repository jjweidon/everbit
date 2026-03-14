package com.everbit.everbit.trade.application;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 대시보드 자산 요약. SoT: docs/api/contracts.md §2 equity 필드.
 * 금액은 문자열(KRW)로 API에 전달하므로 서비스에서는 BigDecimal로 다룸.
 */
@Getter
@Builder
public class DashboardEquitySummary {

	private final BigDecimal equityKrw;
	private final BigDecimal realizedPnlKrw;
	private final BigDecimal unrealizedPnlKrw;
}
