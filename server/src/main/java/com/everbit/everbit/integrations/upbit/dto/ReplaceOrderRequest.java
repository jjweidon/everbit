package com.everbit.everbit.integrations.upbit.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * Upbit POST /v1/orders/cancel_and_new 요청 DTO.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ReplaceOrderRequest(
	String prevOrderUuid,
	String prevOrderIdentifier,
	String newOrdType,
	String newVolume,
	String newPrice,
	String newSmpType,
	String newIdentifier,
	String newTimeInForce
) {
	public ReplaceOrderRequest {
		if (prevOrderUuid == null && prevOrderIdentifier == null) {
			throw new IllegalArgumentException("Either prev_order_uuid or prev_order_identifier must be provided");
		}
	}
}
