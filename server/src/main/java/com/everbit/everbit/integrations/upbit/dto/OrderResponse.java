package com.everbit.everbit.integrations.upbit.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Upbit 주문 API 응답 DTO (GET /v1/order, POST /v1/orders, DELETE /v1/order).
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OrderResponse(
	String uuid,
	String side,
	String ordType,
	String price,
	String state,
	String market,
	OffsetDateTime createdAt,
	String volume,
	String remainingVolume,
	String reservedFee,
	String remainingFee,
	String paidFee,
	String locked,
	String executedVolume,
	String executedFunds,
	Integer tradesCount,
	String preventedVolume,
	String preventedLocked,
	List<Trade> trades,
	String identifier,
	String newOrderUuid
) {
	@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
	public record Trade(
		String market,
		String uuid,
		String price,
		String volume,
		String funds,
		String side
	) {}
}
